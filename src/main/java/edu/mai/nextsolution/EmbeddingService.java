package edu.mai.nextsolution;

import ai.onnxruntime.OrtException;

/**
 * Общий контракт для сервисов, превращающих строку (ФИО) в нормализованный вектор.
 * Реализации: {@link LaBseEmbeddingService} (768d), {@link BgeM3EmbeddingService} (1024d).
 */
public interface EmbeddingService {

    /**
     * @param text предварительно очищенная и нормализованная строка
     * @return L2-нормализованный вектор; его размерность зависит от модели
     */
    float[] getEmbedding(String text) throws OrtException;
}
