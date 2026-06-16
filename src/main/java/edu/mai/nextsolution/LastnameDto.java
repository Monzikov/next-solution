package edu.mai.nextsolution;

public class LastnameDto {
    private final String lastName;
    private final Double similarity;


    // Конструктор
    public LastnameDto(String lastName, Double lcsOut) {
        this.lastName = lastName;
        this.similarity = lcsOut;
    }

    // Геттеры
    public String getLastName() { return lastName; }
    public Double getSimilarity() { return similarity; }
}
