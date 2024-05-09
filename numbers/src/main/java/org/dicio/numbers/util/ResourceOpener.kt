package org.dicio.numbers.util

import java.io.FileNotFoundException
import java.io.InputStream

object ResourceOpener {
    @JvmStatic
    @Throws(FileNotFoundException::class)
    fun getResourceAsStream(path: String): InputStream {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val inputStream = ResourceOpener::class.java.getResourceAsStream(normalizedPath)
            ?: throw FileNotFoundException()

        return inputStream
    }
}
