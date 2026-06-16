package edu.mai.nextsolution;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StopListChecker {

    private final FioEmbeddingService embeddingService;
    private final QdrantClient qdrantClient;
    private final String collectionName;

    private static final float THRESHOLD_LOW = 0.82f;
    private static final float THRESHOLD_HIGH = 0.92f;

    public StopListChecker(FioEmbeddingService embeddingService, QdrantClient qdrantClient,
                           @Value("${app.qdrant.collection-name:stop_list_collection}") String collectionName) {
        this.embeddingService = embeddingService;
        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;
    }

    public CheckResult checkClient(SearchRequest client) {
        try {
            String fullName = String.format("%s %s %s", 
                client.getLastName(), client.getFirstName(), client.getPatronymic()).trim();
            
            float[] vector = embeddingService.getEmbedding(fullName);
            
            List<Points.ScoredPoint> results = qdrantClient.searchAsync(
                Points.SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(floatToFloatList(vector))
                    .setLimit(1)
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build()
            ).get();

            if (results.isEmpty() || results.get(0).getScore() < THRESHOLD_LOW) {
                return new CheckResult(CheckResult.Status.APPROVED, "Авто-пропуск. Совпадений не найдено.");
            }

            Points.ScoredPoint match = results.get(0);
            String stopFio = extractFio(match);

            if (match.getScore() >= THRESHOLD_HIGH) {
                return new CheckResult(CheckResult.Status.REJECTED, 
                    "Авто-блок! Найдено критическое совпадение со стоп-листом: " + stopFio);
            }

            return new CheckResult(CheckResult.Status.MANUAL_REVIEW, 
                "Подозрение на совпадение (" + match.getScore() + ") со стоп-листом: " + stopFio);

        } catch (Exception e) {
            return new CheckResult(CheckResult.Status.MANUAL_REVIEW, "Ошибка проверки: " + e.getMessage());
        }
    }

    private List<Float> floatToFloatList(float[] array) {
        java.util.ArrayList<Float> list = new java.util.ArrayList<>(array.length);
        for (float f : array) list.add(f);
        return list;
    }

    private String extractFio(Points.ScoredPoint point) {
        Map<String, JsonWithInt.Value> payload = point.getPayloadMap();
        if (payload.containsKey("fio")) {
            return payload.get("fio").getStringValue();
        }
        return "Unknown";
    }
}
