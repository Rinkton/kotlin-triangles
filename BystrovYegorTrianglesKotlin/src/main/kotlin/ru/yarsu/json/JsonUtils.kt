package ru.yarsu.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.node.ObjectNode
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.mapper
import ru.yarsu.enums.Color
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

class LowLevelJson {
    public val stringWriter: StringWriter
    public val outputGenerator: JsonGenerator

    init {
        val factory: JsonFactory = JsonFactoryBuilder().build()
        stringWriter = StringWriter()
        outputGenerator = factory.createGenerator(stringWriter)
        outputGenerator.prettyPrinter = DefaultPrettyPrinter()
    }
}

class JsonUtils {
    companion object {
        // Parses json node, check for all the problems. If no problems returns null
        fun getErrorJson(
            json: JsonNode,
            params: ArrayList<JsonParseDataObject>,
        ): ObjectNode? {
            val errorJson = mapper.createObjectNode()
            for (param in params) {
                if (json.has(param.fieldName)) {
                    val field = json.get(param.fieldName)
                    if (!getIsObjectNodeIsOfJsonParseType(field, param.jsonParseType)) {
                        val russianTypeNameExpected = param.jsonParseType.v
                        val russianTypeNameActual = "строка"
                        val paramJson =
                            mapper.createObjectNode().apply {
                                put("Value", json.get(param.fieldName).asText())
                                put(
                                    "Error",
                                    String.format(
                                        "Ожидается %s, но получена %s",
                                        russianTypeNameExpected,
                                        russianTypeNameActual,
                                    ),
                                )
                            }
                        errorJson.set<ObjectNode>(param.fieldName, paramJson)
                    }
                } else if (param.required) {
                    val paramJson =
                        mapper.createObjectNode().apply {
                            putNull("Value")
                            put("Error", String.format("В теле запроса отсутствует поле %s", param.fieldName))
                        }
                    errorJson.set<ObjectNode>(param.fieldName, paramJson)
                }
            }
            return if (!errorJson.isEmpty) errorJson else null
        }

        fun getIsObjectNodeIsOfJsonParseType(
            jsonNode: JsonNode,
            jsonParseType: JsonParseType,
        ): Boolean {
            when (jsonParseType) {
                JsonParseType.STRING -> return jsonNode.isTextual
                JsonParseType.NUMBER -> return jsonNode.isNumber
                JsonParseType.BOOLEAN -> return jsonNode.isBoolean
                JsonParseType.UUID ->
                    return try {
                        UUID.fromString(jsonNode.asText()) != null
                    } catch (e: IllegalArgumentException) {
                        false
                    }
                JsonParseType.DATE_TIME ->
                    return try {
                        LocalDateTime.parse(jsonNode.asText()) != null
                    } catch (e: DateTimeParseException) {
                        false
                    }
                JsonParseType.COLOR ->
                    return try {
                        Color.fromString(jsonNode.asText()) != null
                    } catch (e: IllegalArgumentException) {
                        false
                    }
            }
        }

        fun getJsonNode(request: Request): JsonNode? {
            var json: JsonNode? = null
            try {
                json = request.bodyString().asJsonObject()
            } catch (e: MismatchedInputException) {
                System.err.println("Error: Wasn't able to parse json")
            } catch (e: JsonParseException) {
                System.err.println("Error: Wasn't able to parse json")
            }
            return json
        }

        fun getBodyNotJsonResponse(): Response {
            val lowLevelJson = LowLevelJson()
            with(lowLevelJson.outputGenerator) {
                writeStartObject()
                writeStringField("Value", "{")
                writeStringField("Error", "Missing a name for object member.")
                writeEndObject()
                close()
            }
            return Response(Status.BAD_REQUEST).body(lowLevelJson.stringWriter.toString())
        }

        fun getTriangleNotFoundResponse(triangleIdString: String?): Response {
            val lowLevelJson = LowLevelJson()
            with(lowLevelJson.outputGenerator) {
                writeStartObject()
                writeStringField("TriangleId", triangleIdString)
                writeStringField("Error", "Треугольник не найден")
                writeEndObject()
                close()
            }
            return Response(Status.NOT_FOUND).body(lowLevelJson.stringWriter.toString())
        }

        fun getTemplateNotFoundResponse(templateIdString: String?): Response {
            val lowLevelJson = LowLevelJson()
            with(lowLevelJson.outputGenerator) {
                writeStartObject()
                writeStringField("TemplateId", templateIdString)
                writeStringField("Error", "Шаблон не найден")
                writeEndObject()
                close()
            }
            return Response(Status.NOT_FOUND).body(lowLevelJson.stringWriter.toString())
        }

        fun getUserNotFoundResponse(userIdString: String?): Response {
            val lowLevelJson = LowLevelJson()
            with(lowLevelJson.outputGenerator) {
                writeStartObject()
                writeStringField("UserId", userIdString)
                writeStringField("Error", "Пользователь не найден")
                writeEndObject()
                close()
            }
            return Response(Status.NOT_FOUND).body(lowLevelJson.stringWriter.toString())
        }

        fun getBasicErrorJsonResponse(errorText: String): Response {
            val lowLevelJson = LowLevelJson()
            with(lowLevelJson.outputGenerator) {
                writeStartObject()
                writeStringField("Error", errorText)
                writeEndObject()
                close()
            }
            return Response(Status.BAD_REQUEST).body(lowLevelJson.stringWriter.toString())
        }
    }
}
