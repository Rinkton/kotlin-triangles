package ru.yarsu.storages

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

open class Storage<T>(
    filePath: String,
    protected val items: ArrayList<T>
) {
    init {
        val file = getFile(filePath)
        val dicts: List<Map<String, String>> = csvReader().readAllWithHeader(file)
        try {
            fillUpStoragesWithDicts(dicts)
        } catch (e: Exception) {
            System.err.println(e.message)
        }
    }

    open fun fillUpStoragesWithDicts(dicts: List<Map<String, String>>) {}

    protected fun getNaturalNumber(v: String?): Int {
        if (v == null) throw IllegalArgumentException("v is null")
        val number = v.toInt()
        if (number < 1) throw IllegalArgumentException(String.format("v isn't natural. v: %s", v))
        return number
    }

    private fun getFile(filePathStr: String): File {
        val filePath = Paths.get(filePathStr)
        if (!Files.exists(filePath)) {
            System.err.printf(String.format("Error: file not found «%s»", filePath))
            exitProcess(-1)
        }
        val file: File = filePath.toFile()
        return file
    }
}
