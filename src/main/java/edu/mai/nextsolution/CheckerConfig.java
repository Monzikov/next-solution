package edu.mai.nextsolution;

import io.qdrant.client.QdrantClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Собирает два экземпляра {@link StopListChecker} — по одному на модель.
 * Имена бинов (labseChecker / bgeChecker) используются для инъекции в {@link SearchController}.
 */
@Configuration
public class CheckerConfig {

    @Bean
    public StopListChecker labseChecker(
            LaBseEmbeddingService embeddingService,
            QdrantClient qdrantClient,
            @Value("${app.qdrant.collection-name:stop_list_collection}") String collectionName,
            @Value("${app.match.labse.threshold-low:0.82}") float thresholdLow,
            @Value("${app.match.labse.threshold-high:0.92}") float thresholdHigh) {
        return new StopListChecker(embeddingService, qdrantClient, collectionName, thresholdLow, thresholdHigh);
    }

    @Bean
    public StopListChecker bgeChecker(
            BgeM3EmbeddingService embeddingService,
            QdrantClient qdrantClient,
            @Value("${app.qdrant.bge-collection-name:stop_list_collection_bge_m3}") String collectionName,
            @Value("${app.match.bge.threshold-low:0.82}") float thresholdLow,
            @Value("${app.match.bge.threshold-high:0.92}") float thresholdHigh) {
        return new StopListChecker(embeddingService, qdrantClient, collectionName, thresholdLow, thresholdHigh);
    }
}
