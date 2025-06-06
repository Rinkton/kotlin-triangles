package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import java.util.*
import kotlin.system.exitProcess

fun main() {
    val args = Args()

    val jCommander =
        JCommander
            .newBuilder()
            .addObject(args)
            .build()

    try {
        // TODO: parse не parse чота и градл не запускается. Как починишь вними словам
        // Которые дипсик написал по поводу огранизации рутов
        jCommander.parse(*args)
    } catch (e: ParameterException) {
        println("Error: ${e.message}")
        return
    } catch (e: Exception) {
        exitProcess(-1)
    }
/*
    val taskStorage: TaskStorage?
    try {
        taskStorage = TaskStorage(args.tasksFile ?: "")
    } catch (e: Exception) {
        exitProcess(-1)
        return
    }
    val categoryStorage: CategoryStorage?
    try {
        categoryStorage = CategoryStorage(args.categoriesFile ?: "")
    } catch (e: Exception) {
        exitProcess(-1)
        return
    }

    val hourInMs = 3600000L
    val jwtTools = JwtTools(args.secret ?: "", "ru.yarsu", hourInMs)

    val userStorage: UserStorage?
    try {
        userStorage = UserStorage(args.usersFile ?: "", jwtTools)
    } catch (e: Exception) {
        exitProcess(-1)
        return
    }

    val app =
        routes(
            "/ping".bind(Method.GET) to {
                Response(Status.OK)
            },
            "/v3/tasks/eisenhower".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val importantString = it.query("important")
                    if (importantString != "true" && importantString != "false") {
                        JsonUtils.getBasicErrorJsonResponse(
                            String.format(
                                "Некорректная важность задачи. " +
                                        "Для параметра important ожидается логическое значение, но получено %s",
                                importantString,
                            ),
                        )
                    } else {
                        val urgentString = it.query("urgent")
                        val important = importantString?.toBooleanStrictOrNull()
                        val urgent = urgentString?.toBooleanStrictOrNull()
                        if (important == null && urgent == null) {
                            JsonUtils.getBasicErrorJsonResponse("Отсутствуют оба параметра important и urgent")
                        } else {
                            val taskDatas = taskStorage.getEisenhowerTaskDatas(important, urgent)
                            paginatedOutputWithResponse(it, ArrayList(taskDatas))
                        }
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/tasks/by-time".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val user = userStorage.getUserById(userId)
                    if (user != null && (user.role == Role.USER || user.role == Role.CATEGORY_MANAGER)) {
                        val timeString = it.query("time")
                        if (timeString == null) {
                            JsonUtils.getBasicErrorJsonResponse(
                                String.format(
                                    "Некорректное значение параметры time. Ожидается дата и время в формате ISO, но получена %s",
                                    timeString,
                                ),
                            )
                        } else {
                            try {
                                val time = LocalDateTime.parse(timeString)
                                val actualTaskDatas = taskStorage.getActualTaskDatas(time)
                                paginatedOutputWithResponse(it, ArrayList(actualTaskDatas))
                            } catch (e: DateTimeParseException) {
                                JsonUtils.getBasicErrorJsonResponse(
                                    String.format(
                                        "Некорректное значение параметры time. Ожидается дата и время в формате ISO, но получена %s",
                                        timeString,
                                    ),
                                )
                            }
                        }
                    } else {
                        Response(Status.UNAUTHORIZED)
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/tasks/statistics".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val user = userStorage.getUserById(userId)
                    if (user != null && (user.role == Role.USER || user.role == Role.CATEGORY_MANAGER)) {
                        val byDateString = it.query("by-date")
                        if (byDateString == null) {
                            JsonUtils.getBasicErrorJsonResponse("Отсутствует параметр by-date")
                        } else {
                            try {
                                val byDate = getByDateEnum(byDateString)
                                if (byDate == null) {
                                    JsonUtils.getBasicErrorJsonResponse(
                                        String.format(
                                            "Некорректное значение типа статистики. Для параметра by-date ожидается значение типа статистики, но получено %s",
                                            byDateString,
                                        ),
                                    )
                                } else {
                                    taskStorage.getStatisticsByRegistrationDateTime(byDate)
                                }
                            } catch (e: DateTimeParseException) {
                                JsonUtils.getBasicErrorJsonResponse(
                                    String.format(
                                        "Некорректное значение типа статистики. Для параметра by-date ожидается значение типа статистики, но получено %s",
                                        byDateString,
                                    ),
                                )
                            }
                        }
                    } else {
                        Response(Status.UNAUTHORIZED)
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/tasks".bind(Method.GET) to {
                // TODO: Handle that all the names of query parameters are known
                paginatedOutputWithResponse(it, ArrayList(taskStorage.getTasks()))
            },
            "/v3/tasks".bind(Method.POST) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    var json: JsonNode? = null
                    try {
                        json = it.bodyString().asJsonObject()
                    } catch (e: MismatchedInputException) {
                        System.err.println("Error: Wasn't able to parse json")
                    } catch (e: JsonParseException) {
                        System.err.println("Error: Wasn't able to parse json")
                    }
                    if (json != null) {
                        taskStorage.addTask(json, userId, categoryStorage)
                    } else {
                        JsonUtils.getBodyNotJsonResponse()
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/tasks/{task-id}".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val taskIdString = it.path("task-id")
                    if (taskIdString == null) {
                        JsonUtils.getIncorrectIdFieldResponse("null")
                    } else {
                        taskStorage.getTaskById(taskIdString, userStorage, categoryStorage)
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/tasks/{task-id}".bind(Method.PUT) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    var json: JsonNode? = null
                    try {
                        json = it.bodyString().asJsonObject()
                    } catch (e: MismatchedInputException) {
                        System.err.println("Error: Wasn't able to parse json")
                    } catch (e: JsonParseException) {
                        System.err.println("Error: Wasn't able to parse json")
                    }
                    if (json != null) {
                        val taskIdString = it.path("task-id")
                        if (taskIdString == null) {
                            Response(Status.BAD_REQUEST).body("No task id in the request")
                        }
                        taskStorage.putTaskByTaskId(categoryStorage, userStorage, userId, taskIdString ?: "", json)
                    } else {
                        Response(Status.BAD_REQUEST).body("Error: Wasn't able to parse json")
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/categories".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val user = userStorage.getUserById(userId)
                    if (user != null && (user.role == Role.USER || user.role == Role.CATEGORY_MANAGER)) {
                        paginatedOutputWithResponse(it, ArrayList(categoryStorage.getCategories()))
                    } else {
                        Response(Status.UNAUTHORIZED)
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/categories/{category-id}".bind(Method.PUT) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val formBody = it.bodyString()
                    val formData = parseFormData(formBody)
                    var descriptionFormData = formData["Description"]
                    if (descriptionFormData != null) {
                        val description = URLDecoder.decode(descriptionFormData, StandardCharsets.UTF_8.toString())
                        val color = URLDecoder.decode(formData["Color"] ?: "", StandardCharsets.UTF_8.toString())
                        if (description != null && formData["Color"] != null) {
                            val categoryIdString = it.path("category-id")
                            if (categoryIdString == null) {
                                JsonUtils.getBasicErrorJsonResponse(
                                    String.format(
                                        "Некорректное значение переданного параметра %s. Ожидается UUID, но получено текстовое значение",
                                        categoryIdString,
                                    ),
                                )
                            }
                            categoryStorage.putCategoryByCategoryId(categoryIdString ?: "", description, color, userId, userStorage)
                        } else {
                            Response(Status.BAD_REQUEST).body("Error: Wasn't able to parse request body")
                        }
                    } else {
                        Response(Status.BAD_REQUEST).body("Error: Description is null")
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/categories/{category-id}".bind(Method.DELETE) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val categoryIdString = it.path("category-id")
                    if (categoryIdString == null) {
                        JsonUtils.getIncorrectIdFieldResponse("пустая строка")
                    } else {
                        var category: Category? = null
                        try {
                            category = categoryStorage.getCategoryByCategoryId(UUID.fromString(categoryIdString))
                        } catch (e: IllegalArgumentException) {
                            JsonUtils.getIncorrectIdFieldResponse(categoryIdString)
                        }
                        if (category != null) {
                            if (userId == category.owner) {
                                taskStorage.deleteTasksWithThisCategoryId(categoryIdString ?: "")
                                categoryStorage.deleteCategoryByCategoryId(categoryIdString ?: "")
                            } else {
                                JsonUtils.getCategoryForbidden(userId, category)
                            }
                        } else {
                            JsonUtils.getNotFoundCategoryResponse(categoryIdString)
                        }
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/users".bind(Method.GET) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val user = userStorage.getUserById(userId)
                    if (user != null && user.role == Role.USER_MANAGER) {
                        paginatedOutputWithResponse(it, ArrayList(categoryStorage.getCategories()))
                    } else {
                        Response(Status.UNAUTHORIZED)
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
            "/v3/users/{user-id}".bind(Method.DELETE) to {
                var userId = getExtractedUserIdAndValidate(it, jwtTools, userStorage)
                if (userId != null) {
                    val userIdString = it.path("user-id")
                    if (userIdString == null) {
                        JsonUtils.getIncorrectIdFieldResponse("пустая строка")
                    } else {
                        var user: User? = null
                        var incorrectIdField = false
                        try {
                            user = userStorage.getUserById(UUID.fromString(userIdString))
                        } catch (e: IllegalArgumentException) {
                            incorrectIdField = true
                        }
                        if (user != null) {
                            if (user.id == userId || user.role == Role.USER_MANAGER) {
                                val tasksAndCategoriesByUserJsonNode =
                                    getTasksAndCategoriesByUserJsonNode(user, taskStorage, categoryStorage)
                                if (tasksAndCategoriesByUserJsonNode == null) {
                                    userStorage.deleteUser(user)
                                    Response(Status.NO_CONTENT)
                                } else {
                                    Response(Status.FORBIDDEN).body(tasksAndCategoriesByUserJsonNode.toPrettyString())
                                }
                            } else {
                                Response(Status.UNAUTHORIZED)
                            }
                        } else if (incorrectIdField) {
                            JsonUtils.getIncorrectIdFieldResponse(userIdString)
                        } else {
                            JsonUtils.getNotFoundUserResponse(userIdString)
                        }
                    }
                } else {
                    Response(Status.UNAUTHORIZED)
                }
            },
        )

    app.asServer(Jetty(9000)).start()
 */
}
