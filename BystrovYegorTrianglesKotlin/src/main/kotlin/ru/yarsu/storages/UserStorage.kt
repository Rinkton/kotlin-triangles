package ru.yarsu.storages

import ru.yarsu.classes.User
import ru.yarsu.enums.Role
import ru.yarsu.json.JwtTools
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

class UserStorage(
    filePath: String,
    jwtTools: JwtTools,
) : Storage<User>(filePath, ArrayList<User>()) {
    init {
        for (item in items) {
            item.token = jwtTools.createToken(item) ?: ""
            // println(item.token) // TODO: Remove it
        }
    }

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

    fun getIdOfExistingUserWithSameLoginOrId(
        userId: UUID,
        login: String,
    ): UUID? {
        for (user in items) {
            if (user.id == userId || user.login == login) {
                return user.id
            }
        }
        return null
    }

    fun addUser(user: User) {
        items.add(user)
    }

    fun putUser(puttedUser: User) {
        for (user in items) {
            if (user.id == puttedUser.id) {
                items.remove(user)
                items.add(puttedUser)
                break
            }
        }
    }

    fun deleteUser(user: User) {
        items.remove(user)
    }
}
