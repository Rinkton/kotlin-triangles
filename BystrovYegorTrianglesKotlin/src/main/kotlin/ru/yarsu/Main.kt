package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.fasterxml.jackson.databind.JsonNode
import org.http4k.core.*
import org.http4k.format.Jackson.mapper
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import ru.yarsu.routes.trianglesRoutes
import ru.yarsu.routes.userRoutes
import ru.yarsu.ru.yarsu.JwtTools
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val commandLineArgs = Args()

    val jCommander =
        JCommander
            .newBuilder()
            .addObject(commandLineArgs)
            .build()

    try {
        jCommander.parse(*args)
    } catch (e: ParameterException) {
        println("Error: ${e.message}")
        return
    } catch (e: Exception) {
        exitProcess(-1)
    }

    val templateStorage: TemplateStorage?
    try {
        templateStorage = TemplateStorage(commandLineArgs.templatesFile ?: "")
    } catch (e: Exception) {
        exitProcess(-1)
    }

    val triangleStorage: TriangleStorage?
    try {
        triangleStorage = TriangleStorage(commandLineArgs.trianglesFile ?: "")
    } catch (e: Exception) {
        exitProcess(-1)
    }

    val hourInMs = 3600000L
    val jwtTools = JwtTools(commandLineArgs.secret ?: "", "ru.yarsu", hourInMs)

    val userStorage: UserStorage?
    try {
        userStorage = UserStorage(commandLineArgs.usersFile ?: "", jwtTools)
    } catch (e: Exception) {
        exitProcess(-1)
    }

    val app: HttpHandler =
        routes(
            "/ping".bind(Method.GET) to {
                Response(Status.OK)
            },
            trianglesRoutes(templateStorage, triangleStorage, userStorage),
            userRoutes(templateStorage, triangleStorage, userStorage),
        )

    app.asServer(Jetty(9000)).start()
}
