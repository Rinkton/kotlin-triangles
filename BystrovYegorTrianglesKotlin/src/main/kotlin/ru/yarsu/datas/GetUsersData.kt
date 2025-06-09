package ru.yarsu.datas

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

data class GetUsersData constructor(
    @JsonProperty("Id")
    val id: UUID = UUID.randomUUID(),
    @JsonProperty("Login")
    val login: String,
    @JsonProperty("RegistrationDateTime")
    val registrationDateTime: LocalDateTime,
    @JsonProperty("Email")
    val email: String,
)
