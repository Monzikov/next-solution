package edu.mai.nextsolution;

public class SimilarityRequest {
    private String string_1;
    private String string_2;

    public SimilarityRequest(String string1, String string2) {
        string_1 = string1;
        string_2 = string2;
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
}

