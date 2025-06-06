package ru.yarsu

enum class Color(
    val rgb: Int,
    val description: String,
) {
    BLACK(0x000000, "Чёрный"),
    WHITE(0xFFFFFF, "Белый"),
    RED(0xFF0000, "Красный"),
    GREEN(0x00FF00, "Зелёный"),
    BLUE(0x0000FF, "Синий"),
    YELLOW(0xFFFF00, "Желтый"),
    CYAN(0x00FFFF, "Голубой"),
    MAGENTA(0xFF00FF, "Пурпурный"),
    SILVER(0xC0C0C0, "Серебряный"),
    GRAY(0x808080, "Серый"),
    MAROON(0x800000, "Бордовый"),
    OLIVE(0x808000, "Оливковый"),
    DARKGREEN(0x008000, "Тёмно-зелёный"),
    PURPLE(0x800080, "Фиолетовый"),
    TEAL(0x008080, "Бирюзовый"),
    ;

    companion object {
        fun fromRgbString(rgb: String): Color? {
            val intRgb = rgb.replace("#", "").toLong(16).toInt()
            val color: Color? =
                Color.entries.find {
                    it.rgb
                        .toString()
                        .replace("0x", "#")
                        .toInt() == intRgb
                }
            return color
        }
    }
}
