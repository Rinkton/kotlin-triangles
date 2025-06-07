package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.paginatedOutputWithResponse
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
                getTriangles(triangleStorage),
                postTriangle(),
                getTriangleById(),
            ),
)

private fun getTriangles(triangleStorage: TriangleStorage) =
    "".bind(Method.GET) to withErrorHandling {
        // get all datas and pass it to array list then check if it awaits for page and records-per-page
        val getTrianglesDatas = triangleStorage.getTriangles().forEach()
        paginatedOutputWithResponse(it, ArrayList())
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

