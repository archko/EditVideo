package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.archko.editvideo.db.Graph
import com.archko.editvideo.db.VideoProgress
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class HistoryViewModel : ViewModel() {
    val historiesResponse = MutableLiveData<ResponseHandler<List<VideoProgress>>>()
    val deleteResponse = MutableLiveData<ResponseHandler<String>>()

    suspend fun loadHistories() = flow {
        emit(ResponseHandler.Loading)
        try {
            val list = Graph.database.progressDao().getAllProgress()
            emit(ResponseHandler.Success(list))
        } catch (ex: Exception) {
            emit(ResponseHandler.Failure(ex, ex.message))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteProgress(progress: VideoProgress) = flow {
        emit(ResponseHandler.Loading)
        if (progress == null) {
            emit(ResponseHandler.Failure(extra = "Can't delete VideoProgress!"))
        } else {
            try {
                Graph.database.progressDao().deleteProgress(progress)
                emit(ResponseHandler.Success("success"))
                return@flow
            } catch (ex: Exception) {
                emit(ResponseHandler.Failure(ex, ex.message))
            }
            emit(ResponseHandler.Failure(null, "delete failed"))
        }
    }.flowOn(Dispatchers.IO)
        .collectLatest { deleteResponse.value = it }
}