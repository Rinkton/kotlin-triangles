package ru.yarsu.storages

import ru.yarsu.classes.Triangle
import ru.yarsu.enums.Color
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.ArrayList

class TriangleStorage(
    filePath: String,
) : Storage(filePath) {
    private val triangles = ArrayList<Triangle>()

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
            triangles.add(triangle)
        }
    }
}
