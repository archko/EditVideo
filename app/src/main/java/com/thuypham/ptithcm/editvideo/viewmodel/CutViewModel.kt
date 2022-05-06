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

    val cutResponse = MutableLiveData<ResponseHandler<Boolean>>()
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

    fun cutVideo(url: String) = viewModelScope.launch {
        cutResponse.value = ResponseHandler.Loading
        /*fFmpegHelper.executeMergeVideosWithFile(mediaFileList, onSuccess = {
            deleteFileResponse.postValue(ResponseHandler.Success(it.toString()))
            null
        }, onFail = {
            deleteFileResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })*/
    }
}