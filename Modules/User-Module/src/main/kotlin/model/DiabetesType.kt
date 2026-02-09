package model

enum class DiabetesType(val description: String) {
    TYPE_1("typu pierwszego"),
    TYPE_2("typu drugiego"),
    MODY("MODY"),
    LADA("LADA"),
    GESTATIONAL("ciążowa"),
    NONE("brak")
}
