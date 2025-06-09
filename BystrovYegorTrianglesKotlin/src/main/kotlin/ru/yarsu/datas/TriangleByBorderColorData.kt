package ru.yarsu.datas

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

data class TriangleByBorderColorData constructor(
    @JsonProperty("Id")
    val id: UUID = UUID.randomUUID(),
    @JsonProperty("SideA")
    val sideA: Int,
    @JsonProperty("SideB")
    val sideB: Int,
    @JsonProperty("SideC")
    val sideC: Int,
)
