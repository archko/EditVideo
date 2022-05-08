package com.thuypham.ptithcm.editvideo.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.extension.parseFFprobeStream
import com.thuypham.ptithcm.editvideo.model.FFprobeStream
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class CutViewModel() : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val cutResponse = MutableLiveData<ResponseHandler<String>>()
    val videoInfoResponse = MutableLiveData<ResponseHandler<FFprobeStream>>()
    val deleteFileResponse = MutableLiveData<ResponseHandler<Boolean>>()

    fun deleteFile(url: String) = viewModelScope.launch {
        deleteFileResponse.value = ResponseHandler.Loading

        val file = File(url)
        file.run {
            if (exists()) {
                delete()
                deleteFileResponse.value = ResponseHandler.Success(true)
            }
        }
    }

    fun cutVideo(
        width: Int, height: Int, left: Int, top: Int, filePath: String
    ) = viewModelScope.launch {
        cutResponse.value = ResponseHandler.Loading
        /*fFmpegHelper.cutVideo(width, height, left, top, filePath, null, onSuccess = {
            cutResponse.postValue(ResponseHandler.Success(it))
            null
        }, onFail = {
            cutResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })*/
        getVideoInfo(filePath, onSuccess = {
            doCutVideo(width, height, left, top, filePath, it)
            null
        }, onFail = {
            doCutVideo(width, height, left, top, filePath, null)
            null
        })
    }

    private fun doCutVideo(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        filePath: String,
        fFprobeStream: FFprobeStream?
    ) {
        val bitRate = fFprobeStream?.bit_rate ?: ""
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
                        cutResponse.postValue(ResponseHandler.Success(it))
                        null
                    },
                    onFail = {
                        cutResponse.postValue(ResponseHandler.Failure(extra = it))
                        null
                    })
            }
        }
    }

    fun getVideoInfo(
        filePath: String,
        onSuccess: ((fFprobeStream: FFprobeStream) -> Unit?),
        onFail: ((String?) -> Unit?),
    ) = viewModelScope.launch {
        cutResponse.value = ResponseHandler.Loading
        fFmpegHelper.getVideoInfo(filePath, onSuccess = {
            if (!TextUtils.isEmpty(it)) {
                val fFprobeStream: FFprobeStream? = parseFFprobeStream(it)
                Log.d("TAG", "ffprobe:$fFprobeStream")
                if (null != fFprobeStream) {
                    onSuccess.invoke(fFprobeStream)
                } else {
                    onFail.invoke("error")
                }
            }
            null
        }, onFail = {
            cutResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }

    fun getVideoInfo(
        filePath: String
    ) = viewModelScope.launch {
        videoInfoResponse.value = ResponseHandler.Loading
        fFmpegHelper.getVideoInfo(filePath, onSuccess = {
            if (!TextUtils.isEmpty(it)) {
                val fFprobeStream: FFprobeStream? = parseFFprobeStream(it)
                Log.d("TAG", "ffprobe:$fFprobeStream")
                if (null != fFprobeStream) {
                    videoInfoResponse.postValue(ResponseHandler.Success(fFprobeStream))
                } else {
                    videoInfoResponse.postValue(ResponseHandler.Failure(extra = it))
                }
            }
            null
        }, onFail = {
            videoInfoResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }
}