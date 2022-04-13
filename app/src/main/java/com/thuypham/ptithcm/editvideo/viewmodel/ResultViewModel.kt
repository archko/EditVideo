package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.util.FileHelper
import kotlinx.coroutines.launch
import java.io.File

class ResultViewModel(
    private val fileHelper: FileHelper
) : ViewModel() {

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
}