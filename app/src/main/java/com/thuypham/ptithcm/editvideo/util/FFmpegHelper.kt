package com.thuypham.ptithcm.editvideo.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.util.FileHelper.Companion.OUTPUT_FOLDER_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*


class FFmpegHelper constructor(
    private val context: Context,
) {
    private lateinit var outputDir: File
    private val tempDir = context.getDir(FileHelper.TEMP_FOLDER, Context.MODE_PRIVATE)

    init {
        val tempDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        outputDir = File(tempDir, OUTPUT_FOLDER_NAME)
        if (!outputDir.exists()) outputDir.mkdir()
        if (!tempDir.exists()) tempDir.createNewFile()
    }

    companion object {
        const val FRAME_RATE: Int = 5
        const val DEFAULT_WIDTH: Int = 1280
        const val DEFAULT_HEIGHT: Int = 720
        const val DEFAULT_SECOND: Int = 2

        const val TAG = "FFmpegHelper"
    }

    suspend fun splitVideo(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("cut_video")
            val complexCommand = arrayOf(
                "-ss",
                "" + startMs / 1000,
                "-y",
                "-i",
                filePath,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
//                "-preset",
//                "ultrafast",
                outputPath
            )
            val complexCommand1 = arrayOf(
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-accurate_seek",
                "-i",
                filePath,
                "-c",
                "copy",
                "-preset",
                "ultrafast",
                outputPath
            )

            executeCommand(complexCommand1, {
                onSuccess?.invoke(outputPath)
            }, onFail)
        }
    }

    suspend fun cutVideo(
        width: Int, height: Int, left: Int, top: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("cut_video")
            val complexCommand = arrayOf(
                "-i",
                filePath,
                "-strict",
                "-2",
                "-vf",
                "crop=$width:$height:$left:$top",
                "-y",
                "-preset",
                "ultrafast",
                outputPath
            )
            /*val cmd = arrayOf(
                "-y",
                "-ss",
                start,
                "-i",
                inputPath,
                "-t",
                duration,
                "-vf",
                crop,
                outputPath
            )*/

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputPath)
            }, onFail)
        }
    }

    suspend fun extractAudio(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("cut_video")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-an",
                "-r",
                "1",
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                outputPath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputPath)
            }, onFail)
        }
    }

    suspend fun extractImages(
        startMs: Int, endMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            var outputFolder = File(outputDir, "extract_images")
            var fileNo = 0
            while (outputFolder.exists()) {
                fileNo++
                outputFolder = File(outputDir, "extract_images_$fileNo")
            }
            outputFolder.mkdir()
            val imageFile = File(outputFolder, "extract_images_%03d.jpg")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-an",
                "-r",
                "1",
                "-ss",
                "" + startMs / 1000,
                "-t",
                "" + (endMs - startMs) / 1000,
                imageFile.absolutePath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputFolder.absolutePath)
            }, onFail)
        }
    }

    suspend fun extractOneImage(
        startMs: Int, filePath: String,
        onSuccess: ((outputPath: String) -> Unit?)?,
        onFail: ((String?) -> Unit?)?
    ) {
        withContext(Dispatchers.IO) {
            var outputFolder = File(outputDir, "extract_images")
            var fileNo = 0
            while (outputFolder.exists()) {
                fileNo++
                outputFolder = File(outputDir, "extract_images_$fileNo")
            }
            outputFolder.mkdir()
            val imageFile = File(outputFolder, "extract_images_%03d.jpg")
            val complexCommand = arrayOf(
                "-y",
                "-i",
                filePath,
                "-an",
                "-r",
                "1",
                "-ss",
                "" + startMs / 1000,
                "-t",
                "1",
                //"" + (endMs - startMs) / 1000,
                imageFile.absolutePath
            )

            executeCommand(complexCommand, {
                onSuccess?.invoke(outputFolder.absolutePath)
            }, onFail)
        }
    }

    fun getCommandAddAudioToVideo(
        videoPath: String,
        audioPath: String,
        destinationFilePath: String,
    ): Array<String> {

        return arrayOf(
            "-i",
            videoPath,
            "-i",
            audioPath,
            "-filter_complex",
            "[0:1][1] amix=inputs=2:duration=shortest[a]",
            "-map",
            "0:0",
            "-map",
            "[a]",
            "-c:v",
            "copy",
            "-y",
            destinationFilePath
        )
    }

    public fun getOutputVideoPath(fileName: String): String {
        val file = File(outputDir, "${fileName}_${System.currentTimeMillis()}.mp4")
        return file.absolutePath
    }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }

    private fun getDefaultAudioPath(): String {
        val audioPath = "${outputDir.absolutePath}/default_audio.mp3"
        try {
            val audioFile = File(audioPath)
            if (!audioFile.exists()) {
//                val audioRes = R.raw.default_audio
//                val inputStream = context.resources.openRawResource(audioRes)
//                inputStream.toFile(audioPath)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return audioPath
    }

    fun cmdMergeAudioWithVideo(
        videoPath: String,
        audioPath: String = getDefaultAudioPath(),
        outputPath: String
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(videoPath)
            add("-i")
            add(audioPath)
            add("-c:v")
            add("copy")
            add("-map")
            add("0:v")
            add("-map")
            add("1:a")
            add("-shortest")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    private fun cmdImagesToVideo(
        imageTextPath: String,
        outputPath: String,
        videoWidth: Int? = DEFAULT_WIDTH,
        videoHeight: Int? = DEFAULT_HEIGHT,
        audioPath: String? = null,
    ): Array<String> {
        val cmd: ArrayList<String> = arrayListOf(
            "-y",
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            imageTextPath
        )
        if (audioPath != null) {
            cmd.add("-i")
            cmd.add(audioPath)
        }
        cmd.add("-preset")
        cmd.add("ultrafast")
        cmd.add("-vsync")
        cmd.add("vfr")
        cmd.add("-vf")
        cmd.add("scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease,pad=$videoWidth:$videoHeight:(ow-iw)/2:(oh-ih)/2,format=yuv420p")
        cmd.add("-shortest")
        cmd.add(outputPath)
        return cmd.toArray(arrayOfNulls<String>(cmd.size))
    }

    fun mergeVideos(
        mediaFiles: List<MediaFile>,
        videoWidth: Int? = DEFAULT_WIDTH,
        videoHeight: Int? = DEFAULT_HEIGHT,
        outputPath: String,
        secondPerVideo: Int? = DEFAULT_SECOND,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        var query: String? = ""
        var queryAudio: String? = ""
        var index = 0
        mediaFiles.forEach { mediaFile ->
            if (mediaFile.mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")

                query += "[" + index + ":v]" +
                        "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease,pad=$videoWidth:$videoHeight:(ow-iw)/2:(oh-ih)/2,"
//                        "scale=(iw*sar)*max($videoWidth/(iw*sar)\\,$videoHeight/ih):ih*max($videoWidth/(iw*sar)\\,$videoHeight/ih)," +
//                        "crop=$videoWidth:$videoHeight," +
                "trim=0:$secondPerVideo," +
                        "fps=24,setpts=PTS-STARTPTS[v$index];"

                queryAudio += "[v$index]"
                index++
            }
        }

        inputs.apply {
            add("-y")
            add("-f")
            add("lavfi")
            add("-t")
            add("0.1")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add("$query $queryAudio concat=n=$index:v=1:a=0")
            add("-an")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    private fun getPathImageTextFile(mediaFiles: List<MediaFile>, duration: Int): String {
        val imageTextFile = File(tempDir, "images.txt")
        if (!imageTextFile.exists()) {
            try {
                imageTextFile.createNewFile()
            } catch (e: IOException) {
                Log.d(TAG, "Create image file error: ${e.printStackTrace()}")
            }
        }
        val sb = StringBuilder()
        mediaFiles.forEachIndexed { index, mediaFile ->
            sb.append("file '" + mediaFile.path.toString() + "'")
            sb.append("\n")
            sb.append("duration $duration")
            sb.append("\n")
            if (index == mediaFiles.size - 1) {
                sb.append("file '" + mediaFile.path.toString() + "'")
            }
        }
        try {
            val bufferedWriter = BufferedWriter(FileWriter(imageTextFile))
            bufferedWriter.append(sb)
            bufferedWriter.flush()
            bufferedWriter.close()
        } catch (ex: IOException) {
            Log.d(TAG, "Write image file error: ${ex.printStackTrace()}")
        }
        return imageTextFile.absolutePath
    }

    fun createVideoPreviewProject(
        projectId: String,
        mediaFiles: List<MediaFile>,
        onSuccess: ((MediaFile?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        val outputPath = getOutputVideoPath(projectId)
        val imagesVideoPath = getOutputVideoPath("images_video_$projectId")

        // Todo: Update this condition if needed
        // If there are a lot of media files --> decrease the seconds
        // Note: The seconds of each video shouldn't be less than 1
        val secondsPerVideo = if (mediaFiles.size >= 10) 1 else DEFAULT_SECOND

        // Merge image to video first
        // Then merge image vide with other video
        val images = ArrayList<MediaFile>()
        val videos = ArrayList<MediaFile>()
        mediaFiles.forEach { mediaFile ->
            if (mediaFile.isVideo) {
                videos.add(mediaFile)
            } else {
                images.add(mediaFile)
            }
        }

        when {
            // Only video
            images.isNullOrEmpty() -> {
                executeMergeVideo(videos, onSuccess, onFail, secondsPerVideo, outputPath, projectId)
            }
            // Only image
            videos.isNullOrEmpty() -> {
                executeImagesToVideo(
                    images,
                    onSuccess,
                    onFail,
                    secondsPerVideo,
                    outputPath,
                    projectId,
                    getDefaultAudioPath()
                )
            }
            // Both image and video
            else -> {
                executeImagesToVideo(
                    images, {
                        executeMergeVideo(
                            videos,
                            onSuccess,
                            onFail,
                            secondsPerVideo,
                            outputPath,
                            projectId,
                            imagesVideoPath
                        )
                    }, onFail, secondsPerVideo, imagesVideoPath, projectId
                )
            }
        }
    }

    fun executeMergeVideo(
        videos: ArrayList<MediaFile>,
        onSuccess: ((MediaFile?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
        secondsPerVideo: Int,
        outputPath: String,
        projectId: String,
        imagesVideoPath: String? = null
    ) {
        val cmdMergeVideo = cmdMergeVideosWithNewAudio(
            videos,
            outputPath = outputPath,
            secondPerVideo = secondsPerVideo,
            imagesVideoPath = imagesVideoPath
        )

        executeCommand(cmdMergeVideo, onSuccess = {

            // Get info of video created --> cast to MediaFile
            // Todo: Update: get media video info by path
            val currentMillis = System.currentTimeMillis()
            val mediaFile = MediaFile(
                id = currentMillis,
                path = outputPath,
                dateAdded = currentMillis,
                duration = 0,
                mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                displayName = projectId,
            )
            onSuccess?.invoke(mediaFile)
        }, onFail = {
            val fileImageVideo = File(outputPath)
            if (fileImageVideo.exists()) fileImageVideo.delete()
            onFail?.invoke(it)
        })
    }

    fun executeImagesToVideo(
        images: ArrayList<MediaFile>,
        onSuccess: ((MediaFile?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
        secondPerImage: Int,
        outputPath: String,
        projectId: String,
        audioPath: String? = null
    ) {
        val textImagesPath = getPathImageTextFile(images, secondPerImage)
        val cmd = cmdImagesToVideo(textImagesPath, outputPath, audioPath = audioPath)
        executeCommand(cmd, onSuccess = {
            // Get info of video created --> cast to MediaFile
            // Todo: Update: get media video info by path
            val currentMillis = System.currentTimeMillis()
            val mediaFile = MediaFile(
                id = currentMillis,
                path = outputPath,
                dateAdded = currentMillis,
                duration = 0,
                mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                displayName = projectId,
            )
            // Delete temp image text file
            val fileImageText = File(textImagesPath)
            if (fileImageText.exists()) fileImageText.delete()

            onSuccess?.invoke(mediaFile)
        }, onFail = {
            val fileImageVideo = File(outputPath)
            if (fileImageVideo.exists()) fileImageVideo.delete()
            onFail?.invoke(it)
        })
    }


    fun cmdMergeVideosWithNewAudio(
        mediaFiles: List<MediaFile>,
        videoWidth: Int = DEFAULT_WIDTH,
        videoHeight: Int = DEFAULT_HEIGHT,
        outputPath: String,
        secondPerVideo: Int = DEFAULT_SECOND,
        audioPath: String = getDefaultAudioPath(),
        imagesVideoPath: String? = null,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        var query: String? = ""
        var queryAudio: String? = ""
        var index = 0

        // Set video create by images at the first video merge
        imagesVideoPath?.let { imageVidPath ->
            inputs.add("-i")
            inputs.add(imageVidPath)
            query += "[" + index + ":v]" +
                    "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease," +
                    "pad=$videoWidth:$videoHeight:-1:-1,setsar=1," +
                    "fps=24," +
                    "fade=t=in:st=0:d=1,fade=t=out:st=${secondPerVideo.minus(0.5)}:d=1[v$index];"

            queryAudio += "[v$index]"
            index++
        }

        mediaFiles.forEach { mediaFile ->
            if (mediaFile.mediaType == MediaFile.MEDIA_TYPE_VIDEO) {
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")

                query += "[" + index + ":v]" +
                        "scale=$videoWidth:$videoHeight:force_original_aspect_ratio=decrease," +
                        "pad=$videoWidth:$videoHeight:-1:-1,setsar=1," +
                        "trim=0:$secondPerVideo," +
                        "fps=24," +
                        "fade=t=in:st=0:d=1,fade=t=out:st=${secondPerVideo.minus(0.5)}:d=1[v$index];"

                queryAudio += "[v$index]"
                index++
            }
        }

        // Add audio
        inputs.add("-i")
        inputs.add(audioPath)
        inputs.apply {
            add("-y")
            add("-f")
            add("lavfi")
            add("-t")
            add("0.1")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add("$query $queryAudio concat=n=$index:v=1:a=0[vid]")
            add("-map")
            add("[vid]")
            add("-map")
            add("${index}:a")
            add("-c:a")
            add("aac")
            add("-strict")
            add("-2")
            add("-shortest")
            add("-preset")
            add("ultrafast")
            add(outputPath)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    /**
     * ffmpeg -i 1.mp4 -i 2.mp4 -i 3.mp4 -filter_complex "[0:v] [0:a] [1:v] [1:a] [2:v] [2:a] concat=n=3:v=1:a=1 [vv] [aa]" -map "[vv]" -map "[aa]" mergedVideo.mp4
     * 需要视频的音轨一样,不一样的情况还不知道如何处理.
     * 经常是用上面切割,再到这里合并会失败,流异常,因为有可能切割的时候有黑屏
     */
    suspend fun executeMergeVideos(
        mediaFiles: ArrayList<MediaFile>,
        onSuccess: ((MediaFile?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {
            val outputPath = getOutputVideoPath("merge_video")

            val inputs: ArrayList<String> = ArrayList()
            var index = 0
            mediaFiles.forEach { mediaFile ->
                inputs.add("-i")
                inputs.add(mediaFile.path ?: "")
                index++
            }
            inputs.add("-filter_complex \"")
            index = 0
            mediaFiles.forEach { mediaFile ->
                inputs.add("[$index:v]")
                inputs.add("[$index:a]")
                index++
            }
            inputs.add("concat=n=$index:v=1:a=1")
            inputs.add("[vv]")
            inputs.add("[aa] \"")
            inputs.add("-map")
            inputs.add("\"[vv]\"")
            inputs.add("-map")
            inputs.add("\"[aa]\"")
            inputs.add("-c")
            inputs.add("copy")
            inputs.add("-preset")
            inputs.add("ultrafast")
            inputs.add(outputPath)

            val str = inputs.joinToString(" ")
            Log.d("str", str)
            /*inputs.apply {
                add("-y")
                add("-f")
                add("lavfi")
                add("-t")
                add("0.1")
                add("-i")
                add("anullsrc")
                add("-filter_complex")
                add("-an")
                add("-preset")
                add("ultrafast")
                add(outputPath)
            }*/
            //val cmdMergeVideo: Array<String> = inputs.toArray(arrayOfNulls<String>(inputs.size))

            executeCommandString(str, onSuccess = {
                // Get info of video created --> cast to MediaFile
                // Todo: Update: get media video info by path
                val currentMillis = System.currentTimeMillis()
                val mediaFile = MediaFile(
                    id = currentMillis,
                    path = outputPath,
                    dateAdded = currentMillis,
                    duration = 0,
                    mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                    displayName = outputPath,
                )
                onSuccess?.invoke(mediaFile)
            }, onFail = {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) fileImageVideo.delete()
                onFail?.invoke(it)
            })
        }
    }

    /**
     * 没有权限写入sdcard要注意
     */
    private fun writeFile(file: File, content: String) {
        var writer: BufferedWriter? = null
        try {
            file.createNewFile()
            writer =
                BufferedWriter(FileWriter(file))
            writer.write(content)
            writer.flush()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (null != writer) {
                writer.close()
            }
        }
    }

    /**
     * -f concat -safe 0 -i file.txt -c copy mergedVideo.mp4
     * 生成文件,然后输出
     */
    suspend fun executeMergeVideosWithFile(
        mediaFiles: ArrayList<MediaFile>,
        onSuccess: ((String?) -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        withContext(Dispatchers.IO) {
            val outputFile = File(
                context.externalCacheDir,
                "out_${System.currentTimeMillis()}.txt"
            )
            val outputPath = getOutputVideoPath("merge_video")

            val inputs: ArrayList<String> = ArrayList()
            var content = java.lang.StringBuilder()
            mediaFiles.forEach { mediaFile ->
                content.append("file")
                content.append(" '")
                content.append(mediaFile.path)
                content.append("'\n")
            }

            writeFile(outputFile, content.toString())

            inputs.add("-f")
            inputs.add("concat")
            inputs.add("-safe")
            inputs.add("0")
            inputs.add("-i")
            inputs.add(outputFile.absolutePath)
            inputs.add("-c")
            inputs.add("copy")
            inputs.add("-preset")
            inputs.add("ultrafast")
            inputs.add(outputPath)

            val str = inputs.joinToString(" ")
            Log.d("str", str)

            //val cmdMergeVideo: Array<String> = inputs.toArray(arrayOfNulls<String>(inputs.size))

            executeCommandString(str, onSuccess = {
                val currentMillis = System.currentTimeMillis()
                val mediaFile = MediaFile(
                    id = currentMillis,
                    path = outputPath,
                    dateAdded = currentMillis,
                    duration = 0,
                    mediaType = MediaFile.MEDIA_TYPE_VIDEO,
                    displayName = outputPath,
                )
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                onSuccess?.invoke(outputPath)
            }, onFail = {
                val fileImageVideo = File(outputPath)
                if (fileImageVideo.exists()) {
                    fileImageVideo.delete()
                }
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                onFail?.invoke(it)
            })
        }
    }


    /* Execute command*/
    fun executeCommand(
        command: Array<String>,
        onSuccess: (() -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        try {
            FFmpegKit.executeWithArgumentsAsync(
                command,
                { session ->
                    Log.d(TAG, "FFmpeg process exited with state $session")
                    when {
                        ReturnCode.isSuccess(session.returnCode) -> {
                            Log.d(TAG, "onSuccess")
                            onSuccess?.invoke()
                        }
                        ReturnCode.isCancel(session.returnCode) -> {
                            Log.e(TAG, " executeCommand is cancel:${session.failStackTrace}")
                            onFail?.invoke(session.failStackTrace)
                        }
                        session.returnCode != ReturnCode(ReturnCode.CANCEL) ||
                                session.returnCode != ReturnCode(ReturnCode.SUCCESS) -> {
                            Log.e(TAG, " executeCommand fail: ${session.failStackTrace}")
                            val error =
                                if (session.failStackTrace != null) session.failStackTrace else "Some error occur!"
                            onFail?.invoke(error)
                        }
                        else -> {
                            Log.e(TAG, " executeCommand fail:${session.failStackTrace}")
                        }
                    }
                }, {
                    Log.d(TAG, " onProgress $it")
                }, {
                    Log.d(TAG, "onStatistics $it")
                })
        } catch (ex: Error) {
            Log.e(TAG, " executeCommand error:${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        } catch (ex: Exception) {
            Log.e(TAG, "execute error: ${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        }
    }

    fun executeCommandString(
        command: String,
        onSuccess: (() -> Unit?)? = null,
        onFail: ((String?) -> Unit?)? = null,
    ) {
        try {
            FFmpegKit.executeAsync(command,
                { session ->
                    Log.d(TAG, "FFmpeg process exited with state $session")
                    when {
                        ReturnCode.isSuccess(session.returnCode) -> {
                            Log.d(TAG, "onSuccess")
                            onSuccess?.invoke()
                        }
                        ReturnCode.isCancel(session.returnCode) -> {
                            Log.e(TAG, " executeCommand is cancel:${session.failStackTrace}")
                            onFail?.invoke(session.failStackTrace)
                        }
                        session.returnCode != ReturnCode(ReturnCode.CANCEL) ||
                                session.returnCode != ReturnCode(ReturnCode.SUCCESS) -> {
                            Log.e(TAG, " executeCommand fail: ${session.failStackTrace}")
                            val error =
                                if (session.failStackTrace != null) session.failStackTrace else "Some error occur!"
                            onFail?.invoke(error)
                        }
                        else -> {
                            Log.e(TAG, " executeCommand fail:${session.failStackTrace}")
                        }
                    }
                }, {
                    Log.d(TAG, " onProgress $it")
                }, {
                    Log.d(TAG, "onStatistics $it")
                })
        } catch (ex: Error) {
            Log.e(TAG, " executeCommand error:${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        } catch (ex: Exception) {
            Log.e(TAG, "execute error: ${ex.printStackTrace()}")
            onFail?.invoke(ex.message ?: "")
        }
    }

    /* Cancel all process of FFmpegKit*/
    fun cancel() {
        FFmpegKit.cancel()
    }

}

