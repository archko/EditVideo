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
import com.thuypham.ptithcm.editvideo.model.FFprobeStream
import com.thuypham.ptithcm.editvideo.model.MediaFile
import org.json.JSONObject
import java.io.File

fun MediaFile.getPath(context: Context, data: Uri): String? {
    val path: String? = getFilePathByUri(context, data, this)
    Log.d("path", "path:$path")
    return path
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
                    //这种的是拿文件,没有DURATION
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

//由系统文件管理器选择的是,content://com.android.fileexplorer.myprovider/external_files/DCIM/Camera/VID_20211029_091852.mp4 这种的DURATION是没有的
fun getRealPathFromURI(context: Context, contentUri: Uri?, mediaFile: MediaFile): String? {
    var cursor: Cursor? = null
    try {
        val proj = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Video.Media.DURATION)
        cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(column_index)
        val columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
        mediaFile.path = path
        if (columnIndex > -1) {
            mediaFile.duration = cursor.getLong(columnIndex)
        } else {
            mediaFile.duration = 10
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

/**
 * 递归删除文件夹
 *
 * @param dirPath
 * @return
 */
fun deleteDir(dirPath: String?): Boolean {
    var success = false
    if (dirPath.isNullOrEmpty()) {
        return success
    }
    val dir = File(dirPath)
    return if (dir.isDirectory) {
        val files = dir.listFiles()
        if (files != null) {
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    deleteDir(files[i].absolutePath)
                } else {
                    success = success and files[i].delete()
                }
            }
        }
        success = success and dir.delete()
        success
    } else {
        success
    }
}

fun parseFFprobeStream(content: String): FFprobeStream? {
    val jsonObject = JSONObject(content)
    val ja = jsonObject.optJSONArray("streams")
    if (null != ja && ja.length() > 0) {
        val fFprobeStream = FFprobeStream()
        val jo = ja.optJSONObject(0)
        fFprobeStream.width = jo.optInt("width")
        fFprobeStream.height = jo.optInt("height")
        fFprobeStream.r_frame_rate = jo.optString("r_frame_rate")
        fFprobeStream.duration = jo.optString("duration")
        fFprobeStream.bit_rate = jo.optString("bit_rate")
        fFprobeStream.nb_frames = jo.optString("nb_frames")
        fFprobeStream.sample_aspect_ratio = jo.optString("sample_aspect_ratio")
        fFprobeStream.display_aspect_ratio = jo.optString("display_aspect_ratio")
        fFprobeStream.codec_name = jo.optString("codec_name")
        fFprobeStream.codec_type = jo.optString("codec_type")
        fFprobeStream.level = jo.optInt("level")
        return fFprobeStream
    }
    return null
}

