package edu.mai.nextsolution;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;

import java.util.List;
import java.util.Map;

/**
 * Проверка ФИО по стоп-листу в Qdrant. Не привязан к конкретной модели:
 * получает {@link EmbeddingService}, имя коллекции и пороги через конструктор,
 * поэтому одним и тем же классом обслуживаются и LaBSE, и BGE-M3 (бины в {@link CheckerConfig}).
 */
public class StopListChecker {

    private static final int REUSLT_LIMIT = 3;

    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;
    private final String collectionName;
    private final float thresholdLow;
    private final float thresholdHigh;

    public StopListChecker(EmbeddingService embeddingService, QdrantClient qdrantClient,
                           String collectionName, float thresholdLow, float thresholdHigh) {
        this.embeddingService = embeddingService;
        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;
        this.thresholdLow = thresholdLow;
        this.thresholdHigh = thresholdHigh;
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
                    .setLimit(REUSLT_LIMIT)
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build()
            ).get();

            if (results.isEmpty() || results.get(0).getScore() < thresholdLow) {
                return new CheckResult(CheckResult.Status.APPROVED, "Авто-пропуск. Совпадений не найдено.");
            }

            // Все совпадения выше нижнего порога (их не больше REUSLT_LIMIT — это лимит поиска)
            List<Figurant> figurants = toFigurants(results);

            Points.ScoredPoint match = results.get(0);
            String stopFio = extractFio(match);

            if (match.getScore() >= thresholdHigh) {
                return new CheckResult(CheckResult.Status.REJECTED,
                    "Авто-блок! Найдено критическое совпадение со стоп-листом: " + stopFio, figurants);
            }

            return new CheckResult(CheckResult.Status.MANUAL_REVIEW,
                "Подозрение на совпадение (" + match.getScore() + ") со стоп-листом: " + stopFio, figurants);

        } catch (Exception e) {
            return new CheckResult(CheckResult.Status.MANUAL_REVIEW, "Ошибка проверки: " + e.getMessage());
        }
    }

    private List<Float> floatToFloatList(float[] array) {
        java.util.ArrayList<Float> list = new java.util.ArrayList<>(array.length);
        for (float f : array) list.add(f);
        return list;
    }

    private List<Figurant> toFigurants(List<Points.ScoredPoint> points) {
        List<Figurant> figurants = new java.util.ArrayList<>(points.size());
        for (Points.ScoredPoint point : points) {
            if (point.getScore() < thresholdLow) {
                continue; // отбрасываем слабые совпадения, попавшие в выдачу
            }
            Map<String, JsonWithInt.Value> payload = point.getPayloadMap();
            figurants.add(new Figurant(
                payloadString(payload, "sl_id"),
                payloadString(payload, "full_fio"),
                point.getId().getUuid(),
                point.getScore()
            ));
        }
        return figurants;
    }

    private String extractFio(Points.ScoredPoint point) {
        String fio = payloadString(point.getPayloadMap(), "full_fio");
        return fio != null ? fio : "Unknown";
    }

    /** Достаёт значение payload как строку независимо от его типа (string/integer/double/bool). */
    private String payloadString(Map<String, JsonWithInt.Value> payload, String key) {
        JsonWithInt.Value value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value.hasStringValue()) {
            return value.getStringValue();
        }
        if (value.hasIntegerValue()) {
            return String.valueOf(value.getIntegerValue());
        }
        if (value.hasDoubleValue()) {
            return String.valueOf(value.getDoubleValue());
        }
        if (value.hasBoolValue()) {
            return String.valueOf(value.getBoolValue());
        }
        return null;
    }
}
