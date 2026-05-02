/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Note that [JavaFileSystem] is only used by the desktop prototype and this test suite.
 * This test class covers some default implementation in [FileSystem] too.
 *
 * The tests use [TestContext.internalDirectory] to arrange the tests, as it is the only exposed directory not using the
 * [TestContext.fileSystem] because that class/object is under test.
 */
class JavaFileSystemTests : TestContext() {

    fun File.createNewFileAsFileInfo(fileName: String, content: ByteArray = ByteArray(0)): FileInfo {
        return resolveAsFileInfo(fileName).withData(content)
    }

    fun File.resolveAsFileInfo(fileName: String): FileInfo {
        return fileSystem.fileInfo(resolve(fileName))
    }

    @Nested
    inner class FullName {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `fullName returns file name of existing file`(fileName: String) {
            // Arrange
            val file = internalDirectory.createNewFileAsFileInfo(fileName)

            // Act, Assert
            assertAll(
                { assertTrue { fileSystem.exists(file) } },
                { assertEquals(fileName, file.fullName) }
            )
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `fullName returns file name of not existing file`(fileName: String) {
            // Arrange
            val missingFile = internalDirectory.resolveAsFileInfo(fileName)

            // Act, Assert
            assertAll(
                { assertFalse { fileSystem.exists(missingFile) } },
                { assertEquals(fileName, missingFile.fullName) }
            )
        }
    }

    @Nested
    inner class Size {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `size returns file length of existing file`(fileName: String, fileLength: Int) {
            // Arrange
            val bytes = ByteArray(fileLength) { it.toByte() }
            val file = internalDirectory.createNewFileAsFileInfo(fileName, bytes)

            // Act, Assert
            assertAll(
                { assertTrue { fileSystem.exists(file) } },
                { assertEquals(fileLength, file.size.toInt()) }
            )
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `size returns length 0 of not existing file`(fileName: String) {
            // Arrange
            val missingFile = internalDirectory.resolveAsFileInfo(fileName)

            // Act, Assert
            assertAll(
                { assertFalse { fileSystem.exists(missingFile) } },
                { assertEquals(0L, missingFile.size) }
            )
        }
    }

    @Nested
    inner class Delete {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `delete() deletes existing file successfully`(fileName: String) {
            // Arrange
            val file = internalDirectory.createNewFileAsFileInfo(fileName)

            // Act
            fileSystem.delete(file)

            // Assert
            assertFalse { fileSystem.exists(file) }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `delete() throws IOException on not existing file`(fileName: String) {
            // Arrange
            val missingFile = internalDirectory.resolveAsFileInfo(fileName)

            // Act, Assert
            assertThrows<IOException> { fileSystem.delete(missingFile) }
        }
    }

    @Nested
    inner class OpenInputStream {
        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `openInputStream() returns InputStream on existing file`(fileName: String) {
            // Arrange
            val file = internalDirectory.createNewFileAsFileInfo(fileName)

            // Act, Assert
            fileSystem.openInputStream(file).use { inputStream ->
                assertNotNull(inputStream)
            }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `openInputStream() throws FileNotFoundException on not existing file`(fileName: String) {
            // Arrange
            val missingFile = internalDirectory.resolveAsFileInfo(fileName)

            // Act, Assert
            assertThrows<FileNotFoundException> { fileSystem.openInputStream(missingFile) }
        }
    }

    @Nested
    inner class OpenOutputStream {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `openOutputStream() returns OutputStream on existing file`(fileName: String) {
            // Arrange
            val file = internalDirectory.createNewFileAsFileInfo(fileName)

            // Act, Assert
            fileSystem.openOutputStream(file).use { outputStream ->
                assertNotNull(outputStream)
            }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `openOutputStream() returns OutputStream on not existing file`(fileName: String) {
            // Arrange
            val missingFile = internalDirectory.resolveAsFileInfo(fileName)

            // Act, Assert
            fileSystem.openOutputStream(missingFile).use { outputStream ->
                assertNotNull(outputStream)
            }
        }
    }
}
