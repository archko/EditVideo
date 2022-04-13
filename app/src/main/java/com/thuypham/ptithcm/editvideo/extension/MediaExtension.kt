package com.thuypham.ptithcm.editvideo.extension

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import com.thuypham.ptithcm.editvideo.model.MediaFile

fun MediaFile.getPath(context: Context, data: Uri) {
    val path: String? = getFilePathByUri(context, data, this)
    Log.d("path", "path:$path")
}

fun getFilePathByUri(context: Context, uri: Uri, mediaFile: MediaFile): String? {
    var path: String? = null
    // 以 file:// 开头的
    if (ContentResolver.SCHEME_FILE == uri.scheme) {
        path = uri.path
        mediaFile.path = path
        return path
    }
    // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
    if (ContentResolver.SCHEME_CONTENT == uri.scheme && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.DATA, MediaStore.Video.Media.DURATION),
            null,
            null,
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                var columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (columnIndex > -1) {
                    path = cursor.getString(columnIndex)
                    mediaFile.path = path
                }
                columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                if (columnIndex > -1) {
                    mediaFile.duration = cursor.getLong(columnIndex)
                }
            }
            cursor.close()
        }
        return path
    }
    // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
    if (ContentResolver.SCHEME_CONTENT == uri.scheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    mediaFile.path = path
                    mediaFile.duration = 10
                    return path
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                path = getDataColumn(context, contentUri, null, null, mediaFile)
                return path
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                path = getDataColumn(context, contentUri, selection, selectionArgs, mediaFile)
                return path
            }
        } else {
            getRealPathFromURI(context, uri, mediaFile)
        }
    }
    return null
}

fun getRealPathFromURI(context: Context, contentUri: Uri?, mediaFile: MediaFile): String? {
    var cursor: Cursor? = null
    try {
        val proj = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Video.Media.DURATION)
        cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(column_index)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        if (columnIndex > -1) {
            mediaFile.path = path
            mediaFile.duration = cursor.getLong(columnIndex)
        }
        return path
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return null
}

private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?,
    mediaFile: MediaFile
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column, MediaStore.Video.Media.DURATION)
    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(column)
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            if (columnIndex > -1) {
                mediaFile.duration = cursor.getLong(columnIndex)
            }
            return cursor.getString(column_index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}
