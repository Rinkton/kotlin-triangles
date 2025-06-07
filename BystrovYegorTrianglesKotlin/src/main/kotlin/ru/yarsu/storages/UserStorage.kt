package ru.yarsu.storages

import ru.yarsu.classes.User
import ru.yarsu.enums.Role
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

class UserStorage(
    filePath: String,
) : Storage(filePath) {
    private val users = ArrayList<User>()

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
            users.add(user)
        }
    }
}
