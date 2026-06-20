package edu.mai.nextsolution;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Проверка ФИО по стоп-листу в Qdrant. Не привязан к конкретной модели:
 * получает {@link EmbeddingService}, имя коллекции и пороги через конструктор,
 * поэтому одним и тем же классом обслуживаются и LaBSE, и BGE-M3 (бины в {@link CheckerConfig}).
 */
public class StopListChecker {

    private static final int RESULT_LIMIT = 7;

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
        String fullName = String.format("%s %s %s", client.getLastName(), client.getFirstName(), client.getPatronymic()).trim();
        try {
            float[] vector = embeddingService.getEmbedding(fullName);
            
            List<Points.ScoredPoint> results = qdrantClient.searchAsync(
                Points.SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(floatToFloatList(vector))
                    .setLimit(RESULT_LIMIT)
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build()
            ).get();
            List<Figurant> figurants = toFigurants(results);


            if (figurants.isEmpty()) {
                return new CheckResult(fullName, CheckResult.Status.APPROVED, "Авто-пропуск. Совпадений не найдено.");
            }

            for (var match : figurants) {
                if (match.getSimilarity() >= thresholdHigh) {
                    return new CheckResult(fullName, CheckResult.Status.REJECTED,
                            "Авто-блок! Найдено критическое совпадение с фигурантом (uuid:"+match.getUuid()+", "+match.getFullFio()+") из стоп-листа: " + match.getStopListId(), figurants);
                }
                if (match.getSimilarity() >= thresholdLow) {
                    return new CheckResult(fullName, CheckResult.Status.MANUAL_REVIEW,
                            "Подозрение на совпадение совпадение с фигурантом (uuid:"+match.getUuid()+", "+match.getFullFio()+") из стоп-листа: " + match.getStopListId(), figurants);
                }
            }
            return new CheckResult(fullName, CheckResult.Status.APPROVED, "Авто-пропуск. Значимых совпадений не найдено.", figurants);

        } catch (Exception e) {
            return new CheckResult(fullName, CheckResult.Status.MANUAL_REVIEW, "Ошибка проверки: " + e.getMessage());
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
//            if (point.getScore() < thresholdLow) {
//                continue; // отбрасываем слабые совпадения, попавшие в выдачу
//            }
            Map<String, JsonWithInt.Value> payload = point.getPayloadMap();
            figurants.add(new Figurant(
                payloadString(payload, "sl_id"),
                payloadString(payload, "full_fio"),
                point.getId().getUuid(),
                point.getScore()
            ));
        }
        return figurants.stream()
                .sorted(Comparator.comparingDouble(Figurant::getSimilarity).reversed())
                .collect(Collectors.toList());
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
