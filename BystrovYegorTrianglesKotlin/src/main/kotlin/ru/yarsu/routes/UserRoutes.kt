package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.yarsu.classes.User
import ru.yarsu.datas.GetUsersData
import ru.yarsu.enums.Role
import ru.yarsu.json.JsonUtils
import ru.yarsu.json.JwtTools
import ru.yarsu.json.LowLevelJson
import ru.yarsu.paginatedOutputWithResponse
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

fun usersRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools,
) = routes(
    "/v3/users" bind
        routes(
            getUsers(templateStorage, triangleStorage, userStorage, jwtTools),
            postUser(templateStorage, triangleStorage, userStorage, jwtTools),
            putUser(templateStorage, triangleStorage, userStorage, jwtTools),
            deleteUser(templateStorage, triangleStorage, userStorage, jwtTools),
        ),
)

private fun getUsers(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools,
) = "".bind(Method.GET) to
    withErrorHandling {
        val getUsersData =
            userStorage
                .getUsers()
                .map { user ->
                    GetUsersData(
                        user.id,
                        user.login,
                        user.registrationDateTime,
                        user.email,
                    )
                }.sortedWith(compareBy { it.login.toString() })
                .toCollection(ArrayList())
        paginatedOutputWithResponse(it, ArrayList(getUsersData))
    }

private fun postUser(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools,
) = "".bind(Method.POST) to
    withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val formBody = it.bodyString()
            val formData = parseFormData(formBody)
            var loginFormData = formData["Login"]
            if (loginFormData != null) {
                val login = URLDecoder.decode(loginFormData, StandardCharsets.UTF_8.toString())
                if (login != null) {
                    val idOfExistingUserWithSameLoginOrId =
                        userStorage.getIdOfExistingUserWithSameLoginOrId(
                            userId,
                            login,
                        )
                    if (idOfExistingUserWithSameLoginOrId != null) {
                        val lowLevelJson = LowLevelJson()
                        with(lowLevelJson.outputGenerator) {
                            writeStartObject()
                            writeStringField("Id", idOfExistingUserWithSameLoginOrId.toString())
                            writeEndObject()
                            close()
                        }
                        Response(Status.CONFLICT).body(lowLevelJson.stringWriter.toString())
                    } else {
                        val newId = UUID.randomUUID()
                        val user =
                            User(
                                newId,
                                login,
                                LocalDateTime.now(),
                                "",
                                Role.USER,
                            )
                        userStorage.addUser(user)
                        val lowLevelJson = LowLevelJson()
                        with(lowLevelJson.outputGenerator) {
                            writeStartObject()
                            writeStringField("Id", newId.toString())
                            writeEndObject()
                            close()
                        }
                        Response(Status.CREATED).body(lowLevelJson.stringWriter.toString())
                    }
                } else {
                    Response(Status.BAD_REQUEST).body("Error: Wasn't able to parse request body")
                }
            } else {
                Response(Status.BAD_REQUEST).body("Error: Login is null")
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun putUser(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools,
) = "/{user-id}".bind(Method.PUT) to
    withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val puttedUserIdString = it.path("user-id")
            try {
                val puttedUserId = UUID.fromString(puttedUserIdString)
                val userRoleString = it.query("user-role")
                if (userRoleString == null) {
                    JsonUtils.getBasicErrorJsonResponse(
                        "Некорректное значение переданного параметра user-role. " +
                            "Ожидается UUID, но получено текстовое значение",
                    )
                } else {
                    try {
                        val userRole = Role.fromString(userRoleString)
                        val puttedUser = userStorage.getUserById(puttedUserId)
                        if (puttedUser == null) {
                            JsonUtils.getUserNotFoundResponse(puttedUserIdString)
                        } else {
                            val user =
                                User(
                                    puttedUserId,
                                    puttedUser.login,
                                    puttedUser.registrationDateTime,
                                    puttedUser.email,
                                    userRole,
                                )
                            userStorage.putUser(user)
                            Response(Status.NO_CONTENT)
                        }
                    } catch (e: IllegalArgumentException) {
                        JsonUtils.getBasicErrorJsonResponse(
                            "Некорректное значение переданного параметра user-role. " +
                                "Ожидается Role, но получено текстовое значение",
                        )
                    }
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse(
                    "Некорректное значение переданного параметра user-id. " +
                        "Ожидается UUID, но получено текстовое значение",
                )
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun deleteUser(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools,
) = "/{user-id}".bind(Method.DELETE) to
    withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val userToDeleteIdString = it.path("user-id")
            try {
                val userToDeleteId = UUID.fromString(userToDeleteIdString)
                val userToDelete = userStorage.getUserById(userToDeleteId)
                if (userToDelete == null) {
                    JsonUtils.getUserNotFoundResponse(userToDeleteIdString)
                } else {
                    userStorage.deleteUser(userToDelete)
                    Response(Status.NO_CONTENT)
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse(
                    "Некорректное значение переданного параметра user-id. " +
                        "Ожидается UUID, но получено текстовое значение",
                )
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

fun parseFormData(formBody: String): Map<String, String> =
    formBody
        .split("&")
        .associate { param ->
            val parts = param.split("=", limit = 2)
            val name = URLDecoder.decode(parts[0], Charsets.UTF_8.toString())
            val value =
                if (parts.size == 2) {
                    URLDecoder.decode(parts[1], Charsets.UTF_8.toString())
                } else {
                    ""
                }
            name to value
        }
