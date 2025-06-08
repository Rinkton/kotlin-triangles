package ru.yarsu.enums

enum class Type(
    val v: String,
) {
    INCORRECT("Некорректный"),
    SEGMENT("Отрезок"),
    ACUTE_ANGLED("Остроугольный"),
    RIGHT_ANGLED("Прямоугольный"),
    OBTUSE_ANGLED("Тупоугольный"),
    ;

    fun fromString(v: String): Type {
        val type = Type.entries.find { it.v == v }
        if (type == null) {
            // TODO: Check if the enums in these exception messages are correctly outputted
            throw IllegalArgumentException("Invalid type: '$v'. Available types are: ${Type.entries.joinToString() { it.v }}")
        }
        return type
    }
}
