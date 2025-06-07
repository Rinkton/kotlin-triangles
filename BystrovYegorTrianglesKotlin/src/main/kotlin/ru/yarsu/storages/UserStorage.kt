package ru.yarsu.storages

import ru.yarsu.ru.yarsu.JwtTools
import ru.yarsu.classes.User
import ru.yarsu.enums.Role
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

class UserStorage(
    filePath: String,
    jwtTools: JwtTools,
) : Storage<User>(filePath, ArrayList<User>()) {

    override fun fillUpStoragesWithDicts(dicts: List<Map<String, String>>) {
        for (dict in dicts) {
            val user =
                User(
                    UUID.fromString((dict["Id"])),
                    dict["Login"] ?: "",
                    LocalDateTime.parse(dict["RegistrationDateTime"] ?: ""),
                    dict["Email"] ?: "",
                    Role.fromString(dict["Role"] ?: ""),
                )
            items.add(user)
        }
    }

    fun getUsers(): ArrayList<User> = items

    fun getUserByLogin(login: String): User? = items.find { it.login == login }

    fun getUserById(id: UUID?): User? = items.find { it.id == id }
}
