package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.datas.GetUsersData
import ru.yarsu.json.JwtTools
import ru.yarsu.paginatedOutputWithResponse
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage

fun usersRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools
) = routes(
    "/v3/users" bind
            routes(
                getUsers(templateStorage, triangleStorage, userStorage, jwtTools),
            ),
)

private fun getUsers(templateStorage: TemplateStorage,
                         triangleStorage: TriangleStorage,
                         userStorage: UserStorage,
                         jwtTools: JwtTools) =
    "".bind(Method.GET) to withErrorHandling {
        val getUsersData = userStorage.getUsers().map { user ->
            GetUsersData(
                user.id,
                user.login,
                user.registrationDateTime,
                user.email
            )
        }
            .sortedWith(compareBy { it.login.toString() } )
            .toCollection(ArrayList())
        paginatedOutputWithResponse(it, ArrayList(getUsersData))
    }
