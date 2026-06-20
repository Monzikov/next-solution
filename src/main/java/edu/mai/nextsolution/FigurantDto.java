package edu.mai.nextsolution;

public class FigurantDto {
    private final String lastName;
    private final String firstName;
    private final String patronymic;
    private final String stopListId;
    private Double similarity = 0.0;


    // Конструктор
    public FigurantDto(String lastName, String firstName, String patronymic) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.stopListId = null;
    }

    public FigurantDto(String lastName, String firstName, String patronymic,
                       String lastNameEn, String firstNameEn, String patronymicEn,
                       String stopListId, Double similarity) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.stopListId = stopListId;
        this.similarity = similarity;

    }

    // Геттеры
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getPatronymic() { return patronymic; }
    public String getStopListId() { return stopListId; }
    public Double getSimilarity() { return similarity; }

}
