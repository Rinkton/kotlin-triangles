package ru.yarsu

import com.fasterxml.jackson.databind.JsonNode
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.mapper

enum class RecordsPerPage(
    val v: Int,
) {
    FIRST(5),
    SECOND(10),
    THIRD(20),
    FOURTH(50),
}

fun paginatedOutputWithResponse(
    request: Request,
    whatToOutput: ArrayList<Any>,
): Response {
    val gotPage = request.query("page")
    try {
        val page = gotPage?.toInt() ?: 1
        val recordsPerPage = checkIfIntIsRecordsPerPage(request.query("records-per-page")?.toIntOrNull() ?: 10)
        val paginatedTasks = whatToOutput.drop(page - 1).take(recordsPerPage)
        val json: JsonNode = mapper.valueToTree(paginatedTasks)
        return Response(Status.OK).body(json.toPrettyString())
    } catch (e: NumberFormatException) {
        return JsonUtils.getBasicErrorJsonResponse(
            String.format("Некорректное значение параметра page. Ожидается натуральное число, но получено %s", gotPage.toString()),
        )
    } catch (e: IllegalArgumentException) {
        return JsonUtils.getBasicErrorJsonResponse(
            String.format("Некорректное значение параметра page. Ожидается натуральное число, но получено %s", gotPage.toString()),
        )
    }
}

fun checkIfIntIsRecordsPerPage(v: Int?): Int {
    val recordsPerPage: RecordsPerPage? = RecordsPerPage.entries.find { it.v == v }
    if (recordsPerPage == null) {
        val errorMessage = "Records per page isn't found in the specified set of values"
        System.err.println(errorMessage)
        throw IllegalArgumentException(errorMessage)
    }
    return recordsPerPage.v
}
