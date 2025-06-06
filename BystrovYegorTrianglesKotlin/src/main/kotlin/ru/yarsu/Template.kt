package ru.yarsu

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

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
        get() = getArea()
    val type: Type
        get() = getType()

    fun getArea(): Double {
        TODO()
    }

    fun getType(): Type {
        TODO()
    }
}
