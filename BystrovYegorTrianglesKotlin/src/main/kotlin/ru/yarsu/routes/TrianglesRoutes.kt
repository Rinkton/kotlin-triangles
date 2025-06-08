package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.mapper
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.json.JsonParseDataObject
import ru.yarsu.json.JsonParseType
import ru.yarsu.json.JsonUtils
import ru.yarsu.datas.GetTrianglesData
import ru.yarsu.paginatedOutputWithResponse
import ru.yarsu.json.JwtTools
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage

fun trianglesRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools
) = routes(
    "/v3/triangles" bind
            routes(
                getTriangles(triangleStorage),
                postTriangle(triangleStorage, userStorage, jwtTools),
                getTriangleById(),
            ),
)

private fun getTriangles(triangleStorage: TriangleStorage) =
    "".bind(Method.GET) to withErrorHandling {
        // get all datas and pass it to array list then check if it awaits for page and records-per-page
        val getTrianglesDatas = triangleStorage.getTriangles().map { triangle ->
            GetTrianglesData(
                triangle.id,
                triangle.description,
                triangle.registrationDateTime
            )
        }.toCollection(ArrayList())
        paginatedOutputWithResponse(it, ArrayList(getTrianglesDatas))
    }

private fun postTriangle(triangleStorage: TriangleStorage, userStorage: UserStorage, jwtTools: JwtTools) =
    "".bind(Method.POST) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val json = JsonUtils.getJsonNode(it)
            if (json != null) {
                val errorJson = JsonUtils.getErrorJson(json, arrayListOf(
                    JsonParseDataObject("Template", true, JsonParseType.UUID),
                    JsonParseDataObject("RegistrationDateTime", false, JsonParseType.DATE_TIME),
                    JsonParseDataObject("BorderColor", true, JsonParseType.COLOR),
                    JsonParseDataObject("FillColor", true, JsonParseType.COLOR),
                    JsonParseDataObject("Description", true, JsonParseType.STRING),
                ))
                if (errorJson == null) {
                    // TODO: make the changes(look notepad)
                    Response(Status.CREATED)
                } else {
                    Response(Status.BAD_REQUEST).body(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorJson))
                }
            } else {
                JsonUtils.getBodyNotJsonResponse()
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun getTriangleById() =
    "/{triangle-id}".bind(Method.GET) to withErrorHandling {
        throw BadRequestException("Nopeeeeeeeee")
        Response(Status.NO_CONTENT)
    }

