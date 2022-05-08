package com.thuypham.ptithcm.editvideo.viewmodel

import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.extension.parseFFprobeStream
import com.thuypham.ptithcm.editvideo.model.FFprobeStream
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class CutViewModel() : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val cutResponse = MutableLiveData<ResponseHandler<String>>()
    val mediaInfoResponse = MutableLiveData<FFprobeStream>()

    /**
     * fetch video info by MediaMetadataRetriever
     */
    suspend fun fetchVideoInfo(uri: String?) =
        flow {
            val fFprobeStream = getVideoBySDK(uri)
            emit(fFprobeStream)
        }.flowOn(Dispatchers.IO)
            .collectLatest { mediaInfoResponse.value = it }

    private fun getVideoBySDK(uri: String?): FFprobeStream {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(File(uri).absolutePath)
        val videoWidth =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val videoHeight =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        val rotationDegrees =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
        val bitRate: String? =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)

        val fFprobeStream = FFprobeStream()
        fFprobeStream.width = videoWidth
        fFprobeStream.height = videoHeight
        if (!TextUtils.isEmpty(bitRate)) {
            fFprobeStream.bit_rate = bitRate!!
        }
        return fFprobeStream
    }

    /**
     * fetch video info by ffmpeg,use callbackFlow.
     */
    suspend fun getVideoInfo(filePath: String?): Unit = callbackFlow<FFprobeStream?> {
        fFmpegHelper.getVideoInfo(filePath!!,
            onSuccess = {
                if (!TextUtils.isEmpty(it)) {
                    val fFprobeStream: FFprobeStream? = parseFFprobeStream(it!!)
                    Log.d("TAG", "ffprobe:$fFprobeStream")
                    trySend(fFprobeStream)
                } else {
                    trySend(getVideoBySDK(filePath))
                }
                null
            },
            onFail = {
                trySend(getVideoBySDK(filePath))
                null
            })

        //这是一个挂起函数, 当 flow 被关闭的时候 block 中的代码会被执行 可以在这里取消接口的注册等
        awaitClose { }
    }.flowOn(Dispatchers.IO)
        .collectLatest {
            mediaInfoResponse.value = it
        }

    /**
     * cut video
     */
    suspend fun cutVideo(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        filePath: String,
        fFprobeStream: FFprobeStream?,
    ) = callbackFlow<ResponseHandler<String>> {
        trySend(ResponseHandler.Loading)
        val bitRate = fFprobeStream?.bit_rate
        fFmpegHelper.cutVideo(
            width,
            height,
            left,
            top,
            filePath,
            bitRate,
            onSuccess = {
                trySend(ResponseHandler.Success(it))
                null
            },
            onFail = {
                trySend(ResponseHandler.Failure(extra = it))
                null
            }
        )
        awaitClose { }
    }.flowOn(Dispatchers.IO)
        .collectLatest {
            cutResponse.value = it
        }
}