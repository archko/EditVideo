package com.thuypham.ptithcm.editvideo.viewmodel

import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.extension.parseFFprobeStream
import com.thuypham.ptithcm.editvideo.model.FFprobeStream
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CutViewModel() : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val cutResponse = MutableSharedFlow<ResponseHandler<String>>()
    val mediaInfoResponse = MutableSharedFlow<FFprobeStream>()

    /**
     * fetch video info by MediaMetadataRetriever
     */
    suspend fun fetchVideoInfo(uri: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
                mediaInfoResponse.emit(fFprobeStream)
            }
        }
    }

    /**
     * fetch video info by ffmpeg
     */
    suspend fun getVideoInfo(
        filePath: String?
    ) = viewModelScope.launch {
        if (!TextUtils.isEmpty(filePath)) {
            fFmpegHelper.getVideoInfo(filePath!!, onSuccess = {
                if (!TextUtils.isEmpty(it)) {
                    val fFprobeStream: FFprobeStream? = parseFFprobeStream(it)
                    Log.d("TAG", "ffprobe:$fFprobeStream")
                    if (null != fFprobeStream) {
                        viewModelScope.launch {
                            mediaInfoResponse.emit(fFprobeStream)
                        }
                    } else {
                        viewModelScope.launch {
                            fetchVideoInfo(filePath)
                        }
                    }
                }
                null
            }, onFail = {
                viewModelScope.launch {
                    fetchVideoInfo(filePath)
                }
                null
            })
        }
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
    ) = viewModelScope.launch {
        cutResponse.emit(ResponseHandler.Loading)

        val bitRate = fFprobeStream?.bit_rate
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fFmpegHelper.cutVideo(
                    width,
                    height,
                    left,
                    top,
                    filePath,
                    bitRate,
                    onSuccess = {
                        viewModelScope.launch { cutResponse.emit(ResponseHandler.Success(it)) }
                        null
                    },
                    onFail = {
                        viewModelScope.launch { cutResponse.emit(ResponseHandler.Failure(extra = it)) }
                        null
                    })
            }
        }
    }
}