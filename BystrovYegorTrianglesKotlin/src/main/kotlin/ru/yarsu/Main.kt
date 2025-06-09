package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import ru.yarsu.routes.trianglesRoutes
import ru.yarsu.routes.userRoutes
import ru.yarsu.json.JwtTools
import ru.yarsu.routes.templatesRoutes
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
            templatesRoutes(templateStorage, triangleStorage, userStorage, jwtTools),
            trianglesRoutes(templateStorage, triangleStorage, userStorage, jwtTools),
            userRoutes(templateStorage, triangleStorage, userStorage),
        )

    val server: Http4kServer = app.asServer(Jetty(9000)).start()
        server.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        doBeforeShutdown(templateStorage, triangleStorage, userStorage)
        server.stop()
    })
}

private fun doBeforeShutdown(templateStorage: TemplateStorage,
                             triangleStorage: TriangleStorage,
                             userStorage: UserStorage) {
    val templateRows = arrayListOf(arrayListOf("Id,SideA,SideB,SideC"))
    for (template in templateStorage.getTemplates()) {
        var a = arrayListOf(
            template.id.toString(),
            template.sideA.toString(),
            template.sideB.toString(),
            template.sideC.toString())
        templateRows.add(a)
    }

    val triangleRows = arrayListOf(arrayListOf("Id,Template,RegistrationDateTime,BorderColor,FillColor,Description,Owner"))
    for (triangle in triangleStorage.getTriangles()) {
        var a = arrayListOf(
            triangle.id.toString(),
            triangle.template.toString(),
            triangle.registrationDateTime.toString(),
            triangle.borderColor.toString(),
            triangle.fillColor.toString(),
            triangle.description.toString(),
            triangle.owner.toString(),
        )
        triangleRows.add(a)
    }

    val userRows = arrayListOf(arrayListOf("Id,Login,RegistrationDateTime,Email,Role"))
    for (user in userStorage.getUsers()) {
        var a = arrayListOf(
            user.id.toString(),
            user.login.toString(),
            user.registrationDateTime.toString(),
            user.email.toString(),
            user.role.toString()
        )
        userRows.add(a)
    }

    csvWriter().writeAll(templateRows.toList(), "../sample-data/templates.csv")
    csvWriter().writeAll(triangleRows.toList(), "../sample-data/triangles.csv")
    csvWriter().writeAll(userRows.toList(), "../sample-data/users.csv")
}
