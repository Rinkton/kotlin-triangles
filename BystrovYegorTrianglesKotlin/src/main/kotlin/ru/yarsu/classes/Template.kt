package ru.yarsu.classes

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.enums.Type
import java.lang.Math.pow
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

class Template constructor(
    @JsonProperty("Id")
    val id: UUID,
    @JsonProperty("SideA")
    val sideA: Int, // TODO: must be natural
    @JsonProperty("SideB")
    val sideB: Int,
    @JsonProperty("SideC")
    val sideC: Int,
) {
    val area: Double
        get() {
            val sum = (sideA + sideB + sideC).toDouble()
            val square = sum / 2
            val area = sqrt(square * (square - sideA) * (square - sideB) * (square - sideC))
            return area
        }
    val type: Type
        get() {
            val sides = listOf(sideA, sideB, sideC).sorted()
            val a = sides[0].toDouble()
            val b = sides[1].toDouble()
            val c = sides[2].toDouble() // c - самая большая сторона

            if (a <= 0 || b <= 0 || c <= 0) {
                return Type.INCORRECT
            }

            if (a + b < c) {
                return Type.INCORRECT
            }

            if (a + b == c) {
                return Type.SEGMENT
            }

            val type = when {
                a.pow(2) + b.pow(2) > c.pow(2) -> Type.ACUTE_ANGLED
                a.pow(2) + b.pow(2) == c.pow(2) -> Type.RIGHT_ANGLED
                else -> Type.OBTUSE_ANGLED
            }

            return type
        }
}
