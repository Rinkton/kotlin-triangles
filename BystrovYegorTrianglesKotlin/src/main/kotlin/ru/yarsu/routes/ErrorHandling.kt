package ru.yarsu.routes

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

fun withErrorHandling(handler: HttpHandler): HttpHandler =
    { request ->
        try {
            handler(request)
        } catch (e: BadRequestException) {
            Response(Status.BAD_REQUEST).body(e.message ?: "Bad request")
        } catch (e: NotFoundException) {
            Response(Status.NOT_FOUND).body(e.message ?: "Not found")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body(e.message ?: "")
        }
    }

sealed class HttpException(
    val status: Status,
    message: String,
) : RuntimeException(message)

class BadRequestException(
    message: String = "Bad request",
) : HttpException(Status.BAD_REQUEST, message)

class NotFoundException(
    message: String = "Not found",
) : HttpException(Status.NOT_FOUND, message)
