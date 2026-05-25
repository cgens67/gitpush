package com.example.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class ScannedFile(
    val relativePath: String,
    val base64Content: String,
    val uri: String,
    val name: String,
    val size: Long
)

object FolderScanner {

    suspend fun listAllFiles(context: Context, treeUri: Uri): List<ScannedFile> = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val results = mutableListOf<ScannedFile>()
        scanRecursively(context, rootDoc, "", results)
        results
    }

    private suspend fun scanRecursively(
        context: Context,
        directory: DocumentFile,
        currentPath: String,
        results: MutableList<ScannedFile>
    ) {
        val files = directory.listFiles()
        files.forEach { file ->
            val fileName = file.name ?: return@forEach
            val relativePath = if (currentPath.isEmpty()) fileName else "$currentPath/$fileName"

            if (file.isDirectory) {
                scanRecursively(context, file, relativePath, results)
            } else if (file.isFile) {
                try {
                    val contentResolver = context.contentResolver
                    val base64 = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            val bytes = inputStream.readBytes()
                            Base64.encodeToString(bytes, Base64.NO_WRAP)
                        }
                    } ?: return@forEach
                    results.add(
                        ScannedFile(
                            relativePath = relativePath,
                            base64Content = base64,
                            uri = file.uri.toString(),
                            name = fileName,
                            size = file.length()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
