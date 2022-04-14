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

    val mediaFiles = MutableLiveData<ResponseHandler<ArrayList<MediaFile>>>()
    val mediaSelected = MutableLiveData<ArrayList<MediaFile>>()
    val currentMedia = MutableLiveData<MediaFile>()

    val editVideoResponse = MutableLiveData<ResponseHandler<String>?>()

    fun clearResponse() {
        editVideoResponse.value = null
    }

    fun addVideo() {

    }

}