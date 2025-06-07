package ru.yarsu

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import org.http4k.core.Response
import org.http4k.core.Status
import java.io.StringWriter

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
