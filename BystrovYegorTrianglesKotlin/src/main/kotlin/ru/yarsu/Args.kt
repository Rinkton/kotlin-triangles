package ru.yarsu

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

@Parameters(separators = " ")
open class Args(
    @Parameter(names = ["--templates-file"], description = "Path to the templates file", required = true)
    var templatesFile: String? = null,
    @Parameter(names = ["--triangles-file"], description = "Path to the triangles file", required = true)
    var trianglesFile: String? = null,
    @Parameter(names = ["--users-file"], description = "Path to the users file", required = true)
    var usersFile: String? = null,
    @Parameter(names = ["--port"], description = "Port on which the web server is available", required = true)
    var port: Int? = null,
    @Parameter(names = ["--secret"], required = true)
    var secret: String? = null,
)
