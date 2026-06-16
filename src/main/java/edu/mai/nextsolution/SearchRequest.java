package edu.mai.nextsolution;

public class SearchRequest {
    private String lastName;
    private String firstName;
    private String patronymic;
    ///Общее значение сходства
    private Double similarityMin = 0.0;
    ///Значение сходства для фамилии, если больше общего
    private Double lastNameSimilarityMin = similarityMin;
    ///Значение сходства для имени, если больше общего
    private Double firstNameSimilarityMin = similarityMin;
    ///Значение сходства для отчества, если больше общего
    private Double patronymicSimilarityMin = similarityMin;

    // Геттеры и сеттеры
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public Double getSimilarityMin() { return similarityMin; }
    public void setSimilarityMin(Double similarityMin) { this.similarityMin = similarityMin; }

    public Double getLastNameSimilarityMin() { return lastNameSimilarityMin; }
    public void setLastNameSimilarityMin(Double lastNameSimilarityMin) { this.lastNameSimilarityMin = lastNameSimilarityMin; }

    public Double getFirstNameSimilarityMin() { return firstNameSimilarityMin; }
    public void setFirstNameSimilarityMin(Double firstNameSimilarityMin) { this.firstNameSimilarityMin = firstNameSimilarityMin; }

    public Double getPatronymicSimilarityMin() { return patronymicSimilarityMin; }
    public void setPatronymicSimilarityMin(Double patronymicSimilarityMin) { this.patronymicSimilarityMin = patronymicSimilarityMin; }
}
