package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.mapper
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.yarsu.classes.Triangle
import ru.yarsu.datas.GetTrianglesData
import ru.yarsu.datas.TriangleByAreaData
import ru.yarsu.datas.TriangleByBorderColorData
import ru.yarsu.enums.Color
import ru.yarsu.enums.StatisticsBy
import ru.yarsu.json.*
import ru.yarsu.paginatedOutputWithResponse
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

fun templatesRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools
) = routes(
    "/v3/templates" bind
            routes(
                createTriangleWithTemplate(templateStorage, triangleStorage, userStorage, jwtTools)
            ),
)

private fun createTriangleWithTemplate(templateStorage: TemplateStorage,
                                       triangleStorage: TriangleStorage,
                                       userStorage: UserStorage,
                                       jwtTools: JwtTools) =
    "/{template-id}/triangles".bind(Method.POST) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val json = JsonUtils.getJsonNode(it)
            if (json != null) {
                val templateIdString = it.path("template-id")
                try {
                    val templateId = UUID.fromString(templateIdString ?: "")
                    val template = templateStorage.getTemplateById(templateId)
                    if (template == null) {
                        JsonUtils.getTemplateNotFoundResponse(templateIdString)
                    } else {
                        val errorJson = JsonUtils.getErrorJson(json, arrayListOf(
                            JsonParseDataObject("RegistrationDateTime", false, JsonParseType.DATE_TIME),
                            JsonParseDataObject("BorderColor", true, JsonParseType.COLOR),
                            JsonParseDataObject("FillColor", true, JsonParseType.COLOR),
                            JsonParseDataObject("Description", true, JsonParseType.STRING),
                        ))
                        if (errorJson == null) {
                            val isRegistrationDateTimeSet = json.get("RegistrationDateTime") != null
                            val newId = UUID.randomUUID()
                            val triangle = Triangle(
                                newId,
                                templateId,
                                if (isRegistrationDateTimeSet) LocalDateTime.parse(json.get("RegistrationDateTime").asText())
                                else LocalDateTime.now(),
                                Color.fromString(json.get("BorderColor").asText()),
                                Color.fromString(json.get("FillColor").asText()),
                                json.get("Description").asText(),
                                userId
                            )
                            triangleStorage.addTriangle(triangle)
                            val lowLevelJson = LowLevelJson()
                            with(lowLevelJson.outputGenerator) {
                                writeStartObject()
                                writeStringField("Id", newId.toString())
                                writeEndObject()
                                close()
                            }
                            Response(Status.CREATED).body(lowLevelJson.stringWriter.toString())
                        } else {
                            Response(Status.BAD_REQUEST).body(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorJson))
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра template-id. " +
                            "Ожидается UUID, но получено текстовое значение")
                }
            } else {
                JsonUtils.getBodyNotJsonResponse()
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }
