package edu.mai.nextsolution;

public class PatronymicDto {
    private final String patronymic;
    private Double similarity;


    // Конструктор
    public PatronymicDto(String patronymic, Double lcsOut) {
        this.patronymic = patronymic;
        this.similarity = lcsOut;
    }

    // Геттеры
    public String getPatronymic() { return patronymic; }
    public Double getSimilarity() { return similarity; }
}
