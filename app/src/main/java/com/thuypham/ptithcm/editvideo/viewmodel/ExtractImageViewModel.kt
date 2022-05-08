package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thuypham.ptithcm.editvideo.extension.deleteDir
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class ExtractImageViewModel : ViewModel() {
    val imagesResponse = MutableLiveData<ResponseHandler<ArrayList<MediaFile>>>()
    val deleteImagesResponse = MutableLiveData<ResponseHandler<String>>()

    suspend fun getImageExtracted(folderPath: String?) = flow {
        emit(ResponseHandler.Loading)
        if (folderPath == null) {
            emit(ResponseHandler.Failure(extra = "Cant get image!"))
        } else {
            try {
                val listImagePath = arrayListOf<MediaFile>()
                val fileDir = File(folderPath)
                var count = 0
                for (file in fileDir.walk()) {
                    if (count != 0) {
                        val mediaFile = MediaFile()
                        mediaFile.path = file.path
                        mediaFile.displayName = file.name
                        listImagePath.add(mediaFile)
                    }
                    count++
                }
                imagesResponse.postValue(ResponseHandler.Success(listImagePath))
            } catch (ex: Exception) {
                emit(ResponseHandler.Failure(ex, ex.message))
            }
        }
    }.flowOn(Dispatchers.IO)
        .collectLatest { imagesResponse.value = it }

    suspend fun deleteImage(folderPath: String?) = flow {
        emit(ResponseHandler.Loading)
        if (folderPath == null) {
            emit(ResponseHandler.Failure(extra = "Can't delete image!"))
        } else {
            var flag = false
            try {
                deleteDir(folderPath)
                flag = !File(folderPath).exists()
            } catch (ex: Exception) {
                emit(ResponseHandler.Failure(ex, ex.message))
            }
            if (flag) {
                emit(ResponseHandler.Success("success"))
            } else {
                emit(ResponseHandler.Failure(null, "delete failed"))
            }
        }
    }.flowOn(Dispatchers.IO)
        .collectLatest { deleteImagesResponse.value = it }
}