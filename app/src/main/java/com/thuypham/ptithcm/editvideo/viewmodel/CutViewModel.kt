package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import kotlinx.coroutines.launch
import java.io.File

class CutViewModel(
) : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val cutResponse = MutableLiveData<ResponseHandler<String>>()
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
        fFmpegHelper.cutVideo(width, height, left, top, filePath, onSuccess = {
            cutResponse.postValue(ResponseHandler.Success(it))
            null
        }, onFail = {
            cutResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }
}