package ru.yarsu.routes

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.mapper
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

fun trianglesRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools
) = routes(
    "/v3/triangles" bind
            routes(
                getTrianglesSortedByBorderColor(templateStorage, triangleStorage, userStorage, jwtTools),
                getTrianglesSortedByBorderArea(templateStorage, triangleStorage, userStorage, jwtTools),
                getTrianglesStatistics(templateStorage, triangleStorage, userStorage, jwtTools),
                getTemplates(triangleStorage),
                postTriangle(triangleStorage, userStorage, jwtTools),
                getTriangleById(templateStorage, triangleStorage, userStorage, jwtTools),
                deleteTriangle(templateStorage, triangleStorage, userStorage, jwtTools),
            ),
)

private fun getTemplates(triangleStorage: TriangleStorage) =
    "".bind(Method.GET) to withErrorHandling {
        // get all datas and pass it to array list then check if it awaits for page and records-per-page
        val getTrianglesDatas = triangleStorage.getTriangles().map { triangle ->
            GetTrianglesData(
                triangle.id,
                triangle.description,
                triangle.registrationDateTime
            )
        }
            .sortedWith(compareBy ({ it.registrationDateTime.toString() }, { it.id.toString() }))
            .toCollection(ArrayList())
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
                    val isRegistrationDateTimeSet = json.get("RegistrationDateTime") != null
                    val newId = UUID.randomUUID()
                    val triangle = Triangle(
                        newId,
                        UUID.fromString(json.get("Template").asText()),
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
            } else {
                JsonUtils.getBodyNotJsonResponse()
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun getTriangleById(templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
    jwtTools: JwtTools) =
    "/{triangle-id}".bind(Method.GET) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val triangleIdString = it.path("triangle-id")
            try {
                val triangleId = UUID.fromString(triangleIdString)
                val triangle = triangleStorage.getTriangleById(triangleId)
                if (triangle == null) {
                    JsonUtils.getTriangleNotFoundResponse(triangleIdString)
                } else {
                    val template = templateStorage.getTemplateById(triangle.template)
                    if (template == null) {
                        Response(Status.INTERNAL_SERVER_ERROR).body("Не смогли найти шаблон у " +
                                "указанного вами треугольника. Запрос не может быть выполнен")
                    } else {
                        val owner = userStorage.getUserById(triangle.owner)
                        if (owner == null) {
                            Response(Status.INTERNAL_SERVER_ERROR).body("Не смогли найти владельца у " +
                                    "указанного вами треугольника. Запрос не может быть выполнен")
                        } else {
                            val lowLevelJson = LowLevelJson()
                            with(lowLevelJson.outputGenerator) {
                                writeStartObject()
                                writeStringField("Id", triangle.id.toString())
                                writeStringField("Template", triangle.template.toString())
                                writeNumberField("SideA", template.sideA)
                                writeNumberField("SideB", template.sideB)
                                writeNumberField("SideC", template.sideC)
                                writeStringField("RegistrationDateTime", triangle.registrationDateTime.toString())
                                writeStringField("BorderColor", triangle.borderColor.toString())
                                writeStringField("FillColor", triangle.fillColor.toString())
                                writeStringField("Description", triangle.description)
                                writeNumberField("Area", template.area)
                                writeStringField("Type", template.type.toString())
                                writeStringField("Owner", owner.id.toString())
                                writeStringField("OwnerLogin", owner.login)
                                writeEndObject()
                                close()
                            }
                            Response(Status.OK).body(lowLevelJson.stringWriter.toString())
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра triangle-id. " +
                        "Ожидается UUID, но получено текстовое значение")
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun deleteTriangle(templateStorage: TemplateStorage,
                           triangleStorage: TriangleStorage,
                           userStorage: UserStorage,
                           jwtTools: JwtTools) =
    "/{triangle-id}".bind(Method.DELETE) to withErrorHandling {
        var userId = jwtTools.getExtractedUserIdAndValidate(it, userStorage)
        if (userId != null) {
            val triangleIdString = it.path("triangle-id")
            try {
                val triangleId = UUID.fromString(triangleIdString)
                val triangle = triangleStorage.getTriangleById(triangleId)
                if (triangle == null) {
                    JsonUtils.getTriangleNotFoundResponse(triangleIdString)
                } else {
                    triangleStorage.deleteTriangle(triangle)
                    Response(Status.NO_CONTENT)
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра triangle-id. " +
                        "Ожидается UUID, но получено текстовое значение")
            }
        } else {
            Response(Status.UNAUTHORIZED)
        }
    }

private fun getTrianglesSortedByBorderColor(templateStorage: TemplateStorage,
                                            triangleStorage: TriangleStorage,
                                            userStorage: UserStorage,
                                            jwtTools: JwtTools) =
    "/by-border-color".bind(Method.GET) to withErrorHandling {
        val borderColorString = it.query("border-color")
        try {
            val color = Color.fromString(borderColorString ?: "")
            val triangles = triangleStorage.getTrianglesSortedByBorderColor(color)
            val triangleByBorderColorDatas = ArrayList<TriangleByBorderColorData>()
            for (triangle in triangles) {
                val template = templateStorage.getTemplateById(triangle.template)
                if (template == null) {
                    System.err.println("Нету шаблона, странно")
                } else {
                    triangleByBorderColorDatas.add(TriangleByBorderColorData(
                        triangle.id,
                        template.sideA,
                        template.sideB,
                        template.sideC,
                    ))
                }
            }
            paginatedOutputWithResponse(it, ArrayList(triangleByBorderColorDatas))
        } catch (e: IllegalArgumentException) {
            JsonUtils.getBasicErrorJsonResponse("Некорректное значение переданного параметра border-color. " +
                    "Ожидается Color, но получено текстовое значение")
        }
    }

private fun getTrianglesSortedByBorderArea(templateStorage: TemplateStorage,
                                           triangleStorage: TriangleStorage,
                                           userStorage: UserStorage,
                                           jwtTools: JwtTools) =
    "/by-area".bind(Method.GET) to withErrorHandling {
        val areaMinString = it.query("area-min")
        val areaMaxString = it.query("area-max")
        if (areaMinString == null || areaMaxString == null) {
            JsonUtils.getBasicErrorJsonResponse("Отсутствуют параметры area-min и area-max")
        } else {
            val areaMin = areaMinString.toDoubleOrNull()
            val areaMax = areaMaxString.toDoubleOrNull()
            if (areaMin == null || areaMax == null) {
                if (areaMin == null) {
                    JsonUtils.getBasicErrorJsonResponse(String.format("Некорректное значение нижней границы площади. " +
                            "Для параметра area-min ожидается число, " +
                            "но получено текстовое значение «%s»", areaMinString))
                } else {
                    JsonUtils.getBasicErrorJsonResponse(String.format("Некорректное значение верхней границы площади. " +
                            "Для параметра area-max ожидается число, " +
                            "но получено текстовое значение «%s»", areaMaxString))
                }
            } else {
                val triangles = triangleStorage.getTrianglesSortedByArea(areaMin, areaMax, templateStorage)
                val triangleByAreaDatas = ArrayList<TriangleByAreaData>()
                for (triangle in triangles) {
                    val template = templateStorage.getTemplateById(triangle.template)
                    if (template == null) {
                        System.err.println("Нету шаблона, странно")
                    } else {
                        triangleByAreaDatas.add(TriangleByAreaData(
                            triangle.id,
                            template.sideA,
                            template.sideB,
                            template.sideC,
                        ))
                    }
                }
                paginatedOutputWithResponse(it, ArrayList(triangleByAreaDatas))
            }
        }
    }

private fun getTrianglesStatistics(templateStorage: TemplateStorage,
                                   triangleStorage: TriangleStorage,
                                   userStorage: UserStorage,
                                   jwtTools: JwtTools) =
    "/statistics".bind(Method.GET) to withErrorHandling {
        val byString = it.query("by")
        if (byString == null) {
            JsonUtils.getBasicErrorJsonResponse("Отсутствует параметр by")
        } else {
            try {
                val by = StatisticsBy.fromString(byString)
                when (by) {
                    StatisticsBy.COLOR -> {
                        val lowLevelJson = LowLevelJson()
                        with(lowLevelJson.outputGenerator) {
                            writeStartObject()
                            writeStringField("Value", "{")
                            writeStringField("Error", "Missing a name for object member.")
                            writeEndObject()
                            close()
                        }
                        Response(Status.OK)
                    }
                    StatisticsBy.TYPE -> Response(Status.OK)
                    StatisticsBy.COLOR_TYPE -> Response(Status.OK)
                }
            } catch (e: IllegalArgumentException) {
                JsonUtils.getBasicErrorJsonResponse(String.format("Некорректное значение типа статистики. " +
                        "Для параметра by ожидается значение типа статистики, но получено «%s»", byString))
            }
        }
    }

