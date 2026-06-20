package edu.mai.nextsolution;

public class SimilarityResult {
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        APPROVED, REJECTED, MANUAL_REVIEW
    }
    private String string_1;
    private String string_2;
    private Status status;
    private double similarity;

    public SimilarityResult(String string1, String string2, Status status, double similarity) {
        this.string_1 = string1;
        this.string_2 = string2;
        this.status = status;
        this.similarity = similarity;
    }

    public String getString_1() {
        return string_1;
    }

    public void setString_1(String string_1) {
        this.string_1 = string_1;
    }

    public String getString_2() {
        return string_2;
    }

    public void setString_2(String string_2) {
        this.string_2 = string_2;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
