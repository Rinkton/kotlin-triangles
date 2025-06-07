package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage

fun trianglesRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
) = routes(
    "/v3/triangles" bind
            routes(
                getTriangles(),
                postTriangle(),
                getTriangleById(),
            ),
)

private fun getTriangles() =
    "".bind(Method.GET) to withErrorHandling {
        throw BadRequestException("No")
        Response(Status.NO_CONTENT)
    }

private fun postTriangle() =
    "".bind(Method.POST) to withErrorHandling {
        throw BadRequestException("Nope")
        Response(Status.NO_CONTENT)
    }

private fun getTriangleById() =
    "/{triangle-id}".bind(Method.GET) to withErrorHandling {
        throw BadRequestException("Nopeeeeeeeee")
        Response(Status.NO_CONTENT)
    }

