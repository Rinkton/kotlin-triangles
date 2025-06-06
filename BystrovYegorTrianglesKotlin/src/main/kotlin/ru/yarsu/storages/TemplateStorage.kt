package ru.yarsu.storages

import ru.yarsu.classes.Template
import ru.yarsu.classes.User
import ru.yarsu.enums.Role
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class TemplateStorage(
    filePath: String,
) : Storage(filePath)
{
    private val templates = ArrayList<Template>()

    override fun fillUpStoragesWithDicts(dicts: List<Map<String, String>>) {
        for (dict in dicts) {
            val template =
                Template(
                    UUID.fromString((dict["Id"])),
                    getNaturalNumber(dict["SideA"]),
                    getNaturalNumber(dict["SideB"]),
                    getNaturalNumber(dict["SideC"]),
                )
            templates.add(template)
        }
    }
}
