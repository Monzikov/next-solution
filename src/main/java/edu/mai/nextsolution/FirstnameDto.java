package edu.mai.nextsolution;

public class FirstnameDto {
    private final String firstName;
    private final Double similarity;


    // Конструктор
    public FirstnameDto(String firstName, Double similarityOut) {
        this.firstName = firstName;
        this.similarity = similarityOut;
    }

    // Геттеры
    public String getFirstName() { return firstName; }
    public Double getSimilarity() { return similarity; }
}
