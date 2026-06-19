package edu.mai.nextsolution;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    private static final Logger log = LoggerFactory.getLogger(QdrantConfig.class);

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port:6333}")
    private int port;

    @Value("${spring.ai.vectorstore.qdrant.use-tls:false}")
    private boolean useTls;

    @Value("${app.qdrant.collection-name:stop_list_collection}")
    private String collectionName;

    @Value("${app.qdrant.vector-size:768}")
    private long vectorSize;

    @Value("${app.qdrant.bge-collection-name:stop_list_collection_bge_m3}")
    private String bgeCollectionName;

    @Value("${app.qdrant.bge-vector-size:1024}")
    private long bgeVectorSize;

    @Value("${app.qdrant.distance:Cosine}")
    private String distance;

    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, useTls);
        return new QdrantClient(builder.build());
    }

    /**
     * Проверяет наличие коллекции при старте приложения и создаёт её с нужными
     * параметрами (размер вектора + метрика), если её ещё нет.
     */
    @Bean
    public CommandLineRunner ensureQdrantCollection(QdrantClient qdrantClient) {
        return args -> {
            ensureCollection(qdrantClient, collectionName, vectorSize);     // LaBSE (768)
            ensureCollection(qdrantClient, bgeCollectionName, bgeVectorSize); // BGE-M3 (1024)
        };
    }

    private void ensureCollection(QdrantClient qdrantClient, String name, long size) {
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(name).get();
            if (Boolean.TRUE.equals(exists)) {
                log.info("Коллекция Qdrant '{}' уже существует.", name);
                return;
            }

            Collections.Distance dist = Collections.Distance.valueOf(distance);
            qdrantClient.createCollectionAsync(
                    name,
                    Collections.VectorParams.newBuilder()
                            .setSize(size)
                            .setDistance(dist)
                            .build()
            ).get();
            log.info("Создана коллекция Qdrant '{}' (size={}, distance={}).", name, size, dist);
        } catch (Exception e) {
            log.error("Не удалось создать/проверить коллекцию Qdrant '{}': {}", name, e.getMessage(), e);
        }
    }
}
