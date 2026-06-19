package edu.mai.nextsolution;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Эмбеддинги BGE-M3 (модель XLM-RoBERTa, 1024d).
 *
 * Отличия от {@link LaBseEmbeddingService}:
 *  - модель и токенизатор грузятся не из classpath, а с диска (веса ~2.2 ГБ лежат
 *    рядом в .onnx.data, в jar их класть нельзя);
 *  - у XLM-R только два входа: input_ids + attention_mask (token_type_ids нет);
 *  - вектор берётся как CLS-токен из last_hidden_state[:, 0, :] (у модели нет pooler_output),
 *    затем L2-нормализуется — так же, как при наполнении коллекции в Qdrant.
 */
@Service
public class BgeM3EmbeddingService implements EmbeddingService, AutoCloseable {

    private static final int DIMENSION = 1024;

    private final String modelPath;
    private final String tokenizerPath;

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    public BgeM3EmbeddingService(
            @Value("${app.bge.model-path:inferences/bge_m3_model/bge_m3_model.onnx}") String modelPath,
            @Value("${app.bge.tokenizer-path:inferences/bge_m3_model/tokenizer/tokenizer.json}") String tokenizerPath) {
        this.modelPath = modelPath;
        this.tokenizerPath = tokenizerPath;
    }

    @PostConstruct
    public void init() {
        try {
            java.io.File modelFile = new java.io.File(modelPath);
            if (!modelFile.exists()) {
                System.err.println("[WARNING] BGE-M3 model not found at '" + modelPath
                        + "', BgeM3EmbeddingService will not work properly!");
                return;
            }

            this.env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            options.setInterOpNumThreads(2);
            options.setIntraOpNumThreads(4);
            this.session = env.createSession(modelFile.getAbsolutePath(), options);

            this.tokenizer = loadTokenizer();

            System.out.println("[INFO] BgeM3EmbeddingService initialized successfully with ONNX Runtime.");
        } catch (Throwable t) {
            System.err.println("[ERROR] Failed to initialize ONNX Runtime or load BGE-M3 model. BgeM3EmbeddingService is disabled.");
            t.printStackTrace();
            // Оставляем session == null, чтобы сервис возвращал пустые эмбеддинги вместо падения всего приложения
        }
    }

    private HuggingFaceTokenizer loadTokenizer() throws Exception {
        Path path = Paths.get(tokenizerPath);
        if (!path.toFile().exists()) {
            System.err.println("[ERROR] tokenizer.json для BGE-M3 не найден по пути '" + tokenizerPath
                    + "', токенизация работать не будет!");
            return null;
        }
        Map<String, String> options = new HashMap<>();
        options.put("truncation", "true");
        options.put("maxLength", "512"); // ФИО короткие; ограничиваем, хотя model_max_length=8192
        return HuggingFaceTokenizer.newInstance(path, options);
    }

    /**
     * Генерация векторного эмбеддинга ФИО моделью BGE-M3.
     * @param cleanFio Предварительно очищенная и нормализованная строка ФИО
     * @return Нормализованный вектор float[] размерности 1024 (BGE-M3)
     */
    @Override
    public float[] getEmbedding(String cleanFio) throws OrtException {
        if (session == null || tokenizer == null) {
            return new float[DIMENSION]; // Пустой вектор, если модель/токенизатор не инициализированы
        }

        // 1. Токенизация: XLM-R добавляет спец-токены <s>/</s>, отдаёт input_ids + attention_mask
        Encoding encoding = tokenizer.encode(cleanFio);
        long[] inputIdsArray = encoding.getIds();
        long[] attentionMaskArray = encoding.getAttentionMask();

        int sequenceLength = inputIdsArray.length;
        long[] shape = new long[]{1, sequenceLength}; // [batch_size, sequence_length]

        // 2. Подготовка тензоров. token_type_ids у XLM-R нет — модель его не принимает.
        try (
                OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIdsArray), shape);
                OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMaskArray), shape)
        ) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            // 3. Инференс
            try (OrtSession.Result results = session.run(inputs)) {
                // У BGE-M3 нет pooler_output: dense-эмбеддинг — это CLS-токен (первый)
                // из last_hidden_state ([1, seq_len, 1024]).
                OnnxValue lastHidden = results.get("last_hidden_state")
                        .<OrtException>orElseThrow(() -> new OrtException("В выходах модели нет 'last_hidden_state'"));
                float[][][] hidden = (float[][][]) lastHidden.getValue();
                float[] cls = hidden[0][0];

                // 4. L2-нормализация (для корректного косинусного сравнения с векторами в Qdrant)
                return l2Normalize(cls);
            }
        }
    }

    /** L2-нормализация вектора (нужна для корректного косинусного сравнения). */
    private float[] l2Normalize(float[] vector) {
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += (double) v * v;
        }
        double norm = Math.sqrt(sumSq);
        if (norm == 0.0) {
            return vector;
        }
        float[] out = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            out[i] = (float) (vector[i] / norm);
        }
        return out;
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
        if (tokenizer != null) tokenizer.close();
    }
}
