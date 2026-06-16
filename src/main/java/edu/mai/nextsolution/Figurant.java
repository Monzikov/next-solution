package edu.mai.nextsolution;

/** Найденное совпадение со стоп-листом (из payload Qdrant + score поиска). */
public class Figurant {
    private final String stopListId;   // из payload "sl_id"
    private final String fullFio;      // из payload "full_fio"
    private final String uuid;         // из Point "uuid"
    private final double similarity;   // из ScoredPoint.getScore()

    public Figurant(String stopListId, String fullFio, String uuid, double similarity) {
        this.stopListId = stopListId;
        this.fullFio = fullFio;
        this.uuid = uuid;
        this.similarity = similarity;
    }

    public String getStopListId() { return stopListId; }
    public String getFullFio() { return fullFio; }
    public String getUuid() { return uuid; }
    public double getSimilarity() { return similarity; }
}
