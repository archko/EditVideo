package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FFmpegHelper
import com.thuypham.ptithcm.editvideo.util.IMediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MediaViewModel(
    private val mediaHelper: IMediaHelper,
) : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val mediaFiles = MutableLiveData<ResponseHandler<ArrayList<MediaFile>>>()
    val mediaSelected = MutableLiveData<ArrayList<MediaFile>>()
    val currentMedia = MutableLiveData<MediaFile>()

    val editVideoResponse = MutableLiveData<ResponseHandler<String>?>()

    fun getMedia(mediaType: Int) = viewModelScope.launch {
        mediaFiles.value = ResponseHandler.Loading
        val result = when (mediaType) {
            MediaFile.MEDIA_TYPE_IMAGE -> mediaHelper.getAllImages()
            MediaFile.MEDIA_TYPE_VIDEO -> mediaHelper.getAllVideos()
            MediaFile.MEDIA_TYPE_AUDIO -> mediaHelper.getAllAudio()
            else -> mediaHelper.getAllVideos()
        }
        when (result) {
            is ResponseHandler.Failure -> {
                mediaFiles.value = result
            }
            is ResponseHandler.Success -> {
                if (mediaSelected.value.isNullOrEmpty()) {
                    mediaFiles.value = result
                } else {
                    val tempList = arrayListOf<MediaFile>()
                    result.data.forEach { mediaFile ->
                        mediaSelected.value!!.forEach { selected ->
                            if (mediaFile.id == selected.id) {
                                mediaFile.isSelected = true
                                tempList.add(mediaFile)
                            }
                        }
                    }
                    mediaSelected.value = tempList
                    mediaFiles.value = result
                }
            }
            else -> {

            }
        }
    }

    suspend fun splitVideo(startTime: Float, endTime: Float, filePath: String) = callbackFlow {
        trySend(ResponseHandler.Loading)
        fFmpegHelper.splitVideo(
            startTime.toInt(),
            endTime.toInt(),
            filePath,
            onSuccess = {
                trySend(ResponseHandler.Success(it))
                null
            },
            onFail = {
                trySend(ResponseHandler.Failure(extra = it))
                null
            })

        awaitClose { }
    }.flowOn(Dispatchers.IO).collectLatest {
        editVideoResponse.value = it
    }

    fun extractAudio(startTime: Float, endTime: Float, filePath: String) = viewModelScope.launch {
        editVideoResponse.value = ResponseHandler.Loading
        fFmpegHelper.extractAudio(startTime.toInt(), endTime.toInt(), filePath, onSuccess = {
            editVideoResponse.postValue(ResponseHandler.Success(it))
            null
        }, onFail = {
            editVideoResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }

    suspend fun extractImages(startTime: Float, endTime: Float, filePath: String) = callbackFlow {
        trySend(ResponseHandler.Loading)
        fFmpegHelper.extractImages(
            startTime.toInt(),
            endTime.toInt(),
            filePath,
            onSuccess = {
                trySend(ResponseHandler.Success(it))
                null
            }, onFail = {
                trySend(ResponseHandler.Failure(extra = it))
                null
            })
        awaitClose { }
    }.flowOn(Dispatchers.IO).collectLatest {
        editVideoResponse.value = it
    }

    fun extractOneImage(startTime: Int, filePath: String) = viewModelScope.launch {
        editVideoResponse.value = ResponseHandler.Loading
        fFmpegHelper.extractOneImage(startTime, filePath, onSuccess = {
            editVideoResponse.postValue(ResponseHandler.Success(it))
            null
        }, onFail = {
            editVideoResponse.postValue(ResponseHandler.Failure(extra = it))
            null
        })
    }
}