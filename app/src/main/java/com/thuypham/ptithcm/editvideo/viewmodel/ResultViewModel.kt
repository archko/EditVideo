package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class ResultViewModel(
    private val fileHelper: FileHelper
) : ViewModel() {

    val deleteFileResponse = MutableLiveData<ResponseHandler<Boolean>>()

    suspend fun deleteFile(url: String) = flow {
        emit(ResponseHandler.Loading)
        val file = File(url)
        if (file.exists()) {
            file.delete()
            emit(ResponseHandler.Success(true))
        }
    }.flowOn(Dispatchers.IO)
        .collectLatest { deleteFileResponse.value = it }
}