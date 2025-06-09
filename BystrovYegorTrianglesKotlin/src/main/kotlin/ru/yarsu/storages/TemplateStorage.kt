package ru.yarsu.storages

import ru.yarsu.classes.Template
import java.util.UUID
import kotlin.collections.ArrayList

class TemplateStorage(
    filePath: String,
) : Storage<Template>(filePath, ArrayList<Template>()) {

    override fun fillUpStoragesWithDicts(dicts: List<Map<String, String>>) {
        for (dict in dicts) {
            val template =
                Template(
                    UUID.fromString((dict["Id"])),
                    getNaturalNumber(dict["SideA"]),
                    getNaturalNumber(dict["SideB"]),
                    getNaturalNumber(dict["SideC"]),
                )
            items.add(template)
        }
    }

    fun getTemplateById(template: UUID): Template? {
        return items.find { it.id == template }
    }

    fun getTemplates() = items
}
