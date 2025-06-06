package ru.yarsu

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

class User constructor(
    @JsonProperty("Id")
    val id: UUID = UUID.randomUUID(),
    @JsonProperty("Login")
    val login: String,
    @JsonProperty("RegistrationDateTime")
    val registrationDateTime: LocalDateTime,
    @JsonProperty("Email")
    val email: String,
    @JsonProperty("Role")
    val role: Role,
    var token: String = "",
)
