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

    fun getTemplateById(template: UUID): Template? = items.find { it.id == template }

    fun getTemplates() = items

    fun addTemplate(template: Template) {
        items.add(template)
    }

    fun putTemplate(puttedTemplate: Template) {
        for (template in items) {
            if (template.id == puttedTemplate.id) {
                items.remove(template)
                items.add(puttedTemplate)
                break
            }
        }
    }

    fun getIdOfExistingTemplateWithSameSides(
        sideA: Int,
        sideB: Int,
        sideC: Int,
    ): UUID? {
        val sides = listOf(sideA, sideB, sideC).sorted()
        val a = sides[0]
        val b = sides[1]
        val c = sides[2] // c - самая большая сторона
        for (template in items) {
            val templateSides = listOf(template.sideA, template.sideB, template.sideC).sorted()
            val templateA = templateSides[0]
            val templateB = templateSides[1]
            val templateC = templateSides[2] // templateC - самая большая сторона
            if (templateA == a && templateB == b && templateC == c) {
                return template.id
            }
        }
        return null
    }
}
