package ru.yarsu.json

enum class JsonParseType(val v: String) {
    STRING("строка"),
    NUMBER("число"),
    BOOLEAN("булевое значение"),
    UUID("UUID"),
    DATE_TIME("дата и время"),
    COLOR("цвет из списка"),
}
