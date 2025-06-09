package ru.yarsu.enums

enum class StatisticsBy(
    val v: String,
) {
    COLOR("color"),
    TYPE("type"),
    COLOR_TYPE("color,type"),
    ;

    companion object {
        fun fromString(v: String): StatisticsBy {
            val statisticsBy = StatisticsBy.entries.find { it.v == v }
            if (statisticsBy == null) {
                throw IllegalArgumentException(
                    "Invalid by: '$v'. Available by are: " +
                        "${entries.joinToString { it.v.toString() }}",
                )
            }
            return statisticsBy
        }
    }
}
