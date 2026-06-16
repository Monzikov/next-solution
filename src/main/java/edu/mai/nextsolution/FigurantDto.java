package edu.mai.nextsolution;

public class FigurantDto {
    private final String lastName;
    private final String firstName;
    private final String patronymic;
    private final String lastNameEn;
    private final String firstNameEn;
    private final String patronymicEn;
    private final String stopListId;
    private Double similarity = 0.0;


    // Конструктор
    public FigurantDto(String lastName, String firstName, String patronymic,
                       String lastNameEn, String firstNameEn, String patronymicEn) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.lastNameEn = lastNameEn;
        this.firstNameEn = firstNameEn;
        this.patronymicEn = patronymicEn;
        this.stopListId = null;
    }

    public FigurantDto(String lastName, String firstName, String patronymic,
                       String lastNameEn, String firstNameEn, String patronymicEn,
                       Double lastName_lcs, Double firstName_lcs, Double patronymic_lcs,
                       String stopListId, Double similarity) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.lastNameEn = lastNameEn;
        this.firstNameEn = firstNameEn;
        this.patronymicEn = patronymicEn;
        this.stopListId = stopListId;
        this.similarity = similarity;

    }

    // Геттеры
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getPatronymic() { return patronymic; }
    public String getLastNameEn() { return lastNameEn; }
    public String getFirstNameEn() { return firstNameEn; }
    public String getPatronymicEn() { return patronymicEn; }
    public String getStopListId() { return stopListId; }
    public Double getSimilarity() { return similarity; }

}
