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
        return try {
            Type.valueOf(v)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid type: '$v'. Available types are: ${Type.entries.joinToString()}")
        }
    }
}
