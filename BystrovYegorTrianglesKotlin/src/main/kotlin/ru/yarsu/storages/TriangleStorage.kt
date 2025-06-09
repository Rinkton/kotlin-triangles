package ru.yarsu.storages

import ru.yarsu.classes.Triangle
import ru.yarsu.enums.Color
import ru.yarsu.enums.Type
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

class TriangleStorage(
    filePath: String,
) : Storage<Triangle>(filePath, ArrayList()) {
    override fun fillUpStoragesWithDicts(dicts: List<Map<String, String>>) {
        for (dict in dicts) {
            val triangle =
                Triangle(
                    UUID.fromString((dict["Id"])),
                    UUID.fromString((dict["Template"])),
                    LocalDateTime.parse(dict["RegistrationDateTime"] ?: ""),
                    Color.fromString(dict["BorderColor"] ?: ""),
                    Color.fromString(dict["FillColor"] ?: ""),
                    dict["Description"] ?: "",
                    UUID.fromString((dict["Owner"])),
                )
            items.add(triangle)
        }
    }

    fun getTriangles() = items

    fun addTriangle(triangle: Triangle) {
        items.add(triangle)
    }

    fun getTriangleById(triangleId: UUID): Triangle? = items.find { it.id == triangleId }

    fun deleteTriangle(triangle: Triangle) {
        items.remove(triangle)
    }

    fun getTrianglesSortedByBorderColor(color: Color): ArrayList<Triangle> {
        val trianglesSortedWithBorderColor =
            items
                .filter { it.borderColor == color }
                .sortedWith(compareBy({ it.registrationDateTime }, { it.id }))
        return ArrayList(trianglesSortedWithBorderColor)
    }

    fun getTrianglesSortedByFillColor(color: Color): ArrayList<Triangle> {
        val trianglesSortedWithFillolor =
            items
                .filter { it.fillColor == color }
                .sortedWith(compareBy { it.fillColor.toString() })
        return ArrayList(trianglesSortedWithFillolor)
    }

    fun getTrianglesSortedByArea(
        areaMin: Double,
        areaMax: Double,
        templateStorage: TemplateStorage,
    ): ArrayList<Triangle> {
        val filteredTriangles = ArrayList<Triangle>()
        for (triangle in items) {
            val template = templateStorage.getTemplateById(triangle.template)
            if (template == null) {
                System.err.println("Шаблон не нашёлся, странно")
            } else {
                if (template.area >= areaMin && template.area <= areaMax) {
                    filteredTriangles.add(triangle)
                }
            }
        }
        val filteredSortedTriangles =
            ArrayList(
                filteredTriangles
                    .sortedWith(compareBy({ it.registrationDateTime }, { it.id })),
            )
        return ArrayList(filteredSortedTriangles)
    }

    fun getTrianglesSortedByType(
        type: Type,
        templateStorage: TemplateStorage,
    ): ArrayList<Triangle> {
        val filteredTriangles = ArrayList<Triangle>()
        for (triangle in items) {
            val template = templateStorage.getTemplateById(triangle.template)
            if (template == null) {
                System.err.println("Шаблон не нашёлся, странно")
            } else {
                if (template.type == type) {
                    filteredTriangles.add(triangle)
                }
            }
        }
        return ArrayList(filteredTriangles)
    }
}
