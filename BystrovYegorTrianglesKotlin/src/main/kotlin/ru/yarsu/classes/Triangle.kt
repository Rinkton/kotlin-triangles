package ru.yarsu.classes

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.enums.Color
import java.time.LocalDateTime
import java.util.UUID

class Triangle constructor(
    @JsonProperty("Id")
    val id: UUID,
    @JsonProperty("Template")
    val template: UUID,
    @JsonProperty("RegistrationDateTime")
    val registrationDateTime: LocalDateTime,
    @JsonProperty("BorderColor")
    var borderColor: Color,
    @JsonProperty("FillColor")
    var fillColor: Color,
    @JsonProperty("Description")
    var description: String,
    @JsonProperty("Owner")
    var owner: UUID,
)
