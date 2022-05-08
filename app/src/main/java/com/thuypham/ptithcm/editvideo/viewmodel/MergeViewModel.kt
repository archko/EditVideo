package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class MergeViewModel(
    private val mediaHelper: IMediaHelper,
) : ViewModel() {

    private val fFmpegHelper by lazy { FFmpegHelper(MainApplication.instance) }

    val editVideoResponse = MutableLiveData<ResponseHandler<String>?>()

    suspend fun mergeVideo(mediaFileList: ArrayList<MediaFile>) =
        callbackFlow<ResponseHandler<String>?> {
            trySend(ResponseHandler.Loading)
            fFmpegHelper.executeMergeVideosWithFile(
                mediaFileList,
                onSuccess = {
                    trySend(ResponseHandler.Success(it.toString()))
                    null
                },
                onFail = {
                    trySend(ResponseHandler.Failure(extra = it))
                    null
                })
            awaitClose { }
        }.flowOn(Dispatchers.IO)
            .collectLatest {
                editVideoResponse.value = it
            }
}