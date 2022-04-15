package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import com.thuypham.ptithcm.editvideo.util.IMediaHelper
import kotlinx.coroutines.launch

class MergeViewModel(
    private val mediaHelper: IMediaHelper,
) : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val editVideoResponse = MutableLiveData<ResponseHandler<String>?>()

    fun mergeVideo(mediaFileList: ArrayList<MediaFile>) = viewModelScope.launch {
        editVideoResponse.value = ResponseHandler.Loading
        fFmpegHelper.executeMergeVideos(mediaFileList, onSuccess = {
            editVideoResponse.postValue(ResponseHandler.Success(it.toString()))
            null
        }, onFail = {
            editVideoResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }
}