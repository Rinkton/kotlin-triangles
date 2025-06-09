package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.mapper
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.yarsu.classes.Template
import ru.yarsu.classes.Triangle
import ru.yarsu.datas.GetTemplatesData
import ru.yarsu.enums.Color
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
                createTriangleWithTemplate(templateStorage, triangleStorage, userStorage, jwtTools),
                getTemplates(templateStorage, triangleStorage, userStorage, jwtTools),
                postTemplate(templateStorage, triangleStorage, userStorage, jwtTools),
                getTemplateById(templateStorage, triangleStorage, userStorage, jwtTools),
                putTemplate(templateStorage, triangleStorage, userStorage, jwtTools),
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

private fun getTemplates(templateStorage: TemplateStorage,
                         triangleStorage: TriangleStorage,
                         userStorage: UserStorage,
                         jwtTools: JwtTools) =
    "".bind(Method.GET) to withErrorHandling {
        val getTemplatesDatas = templateStorage.getTemplates().map { template ->
            GetTemplatesData(
                template.id,
                template.sideA,
                template.sideB,
                template.sideC,
            )
        }
            .sortedWith(compareBy { it.id.toString() } )
            .toCollection(ArrayList())
        paginatedOutputWithResponse(it, ArrayList(getTemplatesDatas))
    }

private fun postTemplate(templateStorage: TemplateStorage,
                         triangleStorage: TriangleStorage,
                         userStorage: UserStorage,
                         jwtTools: JwtTools) =
    "".bind(Method.POST) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val json = JsonUtils.getJsonNode(it)
            if (json != null) {
                val errorJson = JsonUtils.getErrorJson(json, arrayListOf(
                    JsonParseDataObject("SideA", true, JsonParseType.NUMBER),
                    JsonParseDataObject("SideB", true, JsonParseType.NUMBER),
                    JsonParseDataObject("SideC", true, JsonParseType.NUMBER),
                ))
                if (errorJson == null) {
                    val idOfExistingTemplateWithSameSides = templateStorage.getIdOfExistingTemplateWithSameSides(
                        json.get("SideA").asText().toInt(),
                        json.get("SideB").asText().toInt(),
                        json.get("SideC").asText().toInt(),
                        )
                    if (idOfExistingTemplateWithSameSides != null) {
                        val lowLevelJson = LowLevelJson()
                        with(lowLevelJson.outputGenerator) {
                            writeStartObject()
                            writeStringField("Id", idOfExistingTemplateWithSameSides.toString())
                            writeEndObject()
                            close()
                        }
                        Response(Status.CONFLICT).body(lowLevelJson.stringWriter.toString())
                    } else {
                        val newId = UUID.randomUUID()
                        val template = Template(
                            newId,
                            json.get("SideA").asText().toInt(),
                            json.get("SideB").asText().toInt(),
                            json.get("SideC").asText().toInt(),
                        )
                        templateStorage.addTemplate(template)
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
                    Response(Status.BAD_REQUEST).body(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorJson))
                }
            } else {
                JsonUtils.getBodyNotJsonResponse()
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun getTemplateById(templateStorage: TemplateStorage,
                            triangleStorage: TriangleStorage,
                            userStorage: UserStorage,
                            jwtTools: JwtTools) =
    "/{template-id}".bind(Method.GET) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val templateIdString = it.path("template-id")
            try {
                val templateId = UUID.fromString(templateIdString)
                val template = templateStorage.getTemplateById(templateId)
                if (template == null) {
                    JsonUtils.getTemplateNotFoundResponse(templateIdString)
                } else {
                    val lowLevelJson = LowLevelJson()
                    with(lowLevelJson.outputGenerator) {
                        writeStartObject()
                        writeStringField("Id", template.id.toString())
                        writeNumberField("SideA", template.sideA)
                        writeNumberField("SideB", template.sideB)
                        writeNumberField("SideC", template.sideC)
                        writeNumberField("Area", template.area)
                        writeStringField("Type", template.type.v)
                        writeEndObject()
                        close()
                    }
                    Response(Status.OK).body(lowLevelJson.stringWriter.toString())
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра template-id. " +
                        "Ожидается UUID, но получено текстовое значение")
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun putTemplate(templateStorage: TemplateStorage,
                        triangleStorage: TriangleStorage,
                        userStorage: UserStorage,
                        jwtTools: JwtTools) =
    "/{template-id}".bind(Method.PUT) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val templateIdString = it.path("template-id")
            try {
                val templateId = UUID.fromString(templateIdString)
                val json = JsonUtils.getJsonNode(it)
                if (json != null) {
                    val errorJson = JsonUtils.getErrorJson(json, arrayListOf(
                        JsonParseDataObject("SideA", true, JsonParseType.NUMBER),
                        JsonParseDataObject("SideB", true, JsonParseType.NUMBER),
                        JsonParseDataObject("SideC", true, JsonParseType.NUMBER),
                    ))
                    if (errorJson == null) {
                        val idOfExistingTemplateWithSameSides = templateStorage.getIdOfExistingTemplateWithSameSides(
                            json.get("SideA").asText().toInt(),
                            json.get("SideB").asText().toInt(),
                            json.get("SideC").asText().toInt(),
                        )
                        if (idOfExistingTemplateWithSameSides != null) {
                            val lowLevelJson = LowLevelJson()
                            with(lowLevelJson.outputGenerator) {
                                writeStartObject()
                                writeStringField("Id", idOfExistingTemplateWithSameSides.toString())
                                writeEndObject()
                                close()
                            }
                            Response(Status.CONFLICT).body(lowLevelJson.stringWriter.toString())
                        } else {
                            val template = Template(
                                templateId,
                                json.get("SideA").asText().toInt(),
                                json.get("SideB").asText().toInt(),
                                json.get("SideC").asText().toInt(),
                            )
                            templateStorage.putTemplate(template)
                            Response(Status.NO_CONTENT)
                        }
                    } else {
                        Response(Status.BAD_REQUEST).body(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorJson))
                    }
                } else {
                    JsonUtils.getBodyNotJsonResponse()
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра template-id. " +
                        "Ожидается UUID, но получено текстовое значение")
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }
