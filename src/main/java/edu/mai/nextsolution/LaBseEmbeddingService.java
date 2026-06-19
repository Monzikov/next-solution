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


@Service
public class LaBseEmbeddingService implements EmbeddingService, AutoCloseable {

    private final String modelPath;
    private final String tokenizerPath;

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    public LaBseEmbeddingService(
            @Value("${app.labse.model-path:inferences/labse/labse_model.onnx}") String modelPath,
            @Value("${app.labse.tokenizer-path:inferences/labse/tokenizer/tokenizer.json}") String tokenizerPath) {
        this.modelPath = modelPath;
        this.tokenizerPath = tokenizerPath;
    }

    @PostConstruct
    public void init() {
        try {
            java.io.File modelFile = new java.io.File(modelPath);
            if (!modelFile.exists()) {
                System.err.println("[WARNING] LaBSE model not found at '" + modelPath
                        + "', LaBseEmbeddingService will not work properly!");
                return;
            }

            this.env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            options.setInterOpNumThreads(2);
            options.setIntraOpNumThreads(4);
            this.session = env.createSession(modelFile.getAbsolutePath(), options);

            // Загружаем реальный токенизатор (HuggingFace fast-tokenizer) с диска
            this.tokenizer = loadTokenizer();

            System.out.println("[INFO] LaBseEmbeddingService initialized successfully with ONNX Runtime.");
        } catch (Throwable t) {
            System.err.println("[ERROR] Failed to initialize ONNX Runtime or load model. LaBseEmbeddingService is disabled.");
            t.printStackTrace();
            // Оставляем session == null, чтобы сервис возвращал пустые эмбеддинги вместо падения всего приложения
        }
    }

    private HuggingFaceTokenizer loadTokenizer() throws Exception {
        Path path = Paths.get(tokenizerPath);
        if (!path.toFile().exists()) {
            System.err.println("[ERROR] tokenizer.json для LaBSE не найден по пути '" + tokenizerPath
                    + "', токенизация работать не будет!");
            return null;
        }
        Map<String, String> options = new HashMap<>();
        options.put("truncation", "true");
        options.put("maxLength", "512"); // model_max_length из tokenizer_config.json
        return HuggingFaceTokenizer.newInstance(path, options);
    }

    /**
     * Генерация векторного эмбеддинга ФИО.
     * @param cleanFio Предварительно очищенная и нормализованная строка ФИО
     * @return Нормализованный вектор float[] размерности 768 (LaBSE)
     */
    @Override
    public float[] getEmbedding(String cleanFio) throws OrtException {
        if (session == null || tokenizer == null) {
            return new float[768]; // Пустой вектор, если модель/токенизатор не инициализированы (размерность LaBSE)
        }

        // 1. Реальная токенизация: добавляются спец-токены [CLS]/[SEP], формируются три входа модели
        Encoding encoding = tokenizer.encode(cleanFio);
        long[] inputIdsArray = encoding.getIds();
        long[] attentionMaskArray = encoding.getAttentionMask();
        long[] tokenTypeIdsArray = encoding.getTypeIds();

        int sequenceLength = inputIdsArray.length;

        // 2. Подготовка тензоров для ONNX
        long[] shape = new long[]{1, sequenceLength}; // [batch_size, sequence_length]

        try (
                OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIdsArray), shape);
                OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMaskArray), shape);
                OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIdsArray), shape)
        ) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
            inputs.put("token_type_ids", tokenTypeIdsTensor);

            // 3. Запуск инференса (расчет на C++ движке)
            try (OrtSession.Result results = session.run(inputs)) {
                // Берём именно pooler_output ([1, 768]) — так же, как при наполнении БД (Python).
                // Это вектор от токена [CLS]; mean pooling по last_hidden_state дал бы ДРУГОЕ
                // пространство и не совпал бы с векторами в Qdrant.
                OnnxValue poolerOutput = results.get("pooler_output")
                        .<OrtException>orElseThrow(() -> new OrtException("В выходах модели нет 'pooler_output'"));
                float[][] pooled = (float[][]) poolerOutput.getValue();

                // 4. Обязательная L2-нормализация для LaBSE
                return l2Normalize(pooled[0]);
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
