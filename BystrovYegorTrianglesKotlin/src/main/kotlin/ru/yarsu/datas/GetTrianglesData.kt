package ru.yarsu.datas

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

data class GetTrianglesData constructor(
    @JsonProperty("Id")
    val id: UUID = UUID.randomUUID(),
    @JsonProperty("Description")
    val description: String,
    @JsonProperty("RegistrationDateTime")
    val registrationDateTime: LocalDateTime,
)
