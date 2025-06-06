package ru.yarsu

enum class Type(
    val v: String,
) {
    INCORRECT("Некорректный"),
    SEGMENT("Отрезок"),
    ACUTE_ANGLED("Остроугольный"),
    RIGHT_ANGLED("Прямоугольный"),
    OBTUSE_ANGLED("Тупоугольный"),
}
