package ru.yarsu.classes

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.enums.Type
import java.util.UUID

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
    private val area: Double
        get() {
            TODO()
        }
    private val type: Type
        get() {
            TODO()
        }
}
