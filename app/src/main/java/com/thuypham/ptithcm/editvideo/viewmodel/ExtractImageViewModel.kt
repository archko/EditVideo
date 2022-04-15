package com.thuypham.ptithcm.editvideo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuypham.ptithcm.editvideo.extension.deleteDir
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ExtractImageViewModel(
) : ViewModel() {
    val imagesResponse = MutableLiveData<ResponseHandler<ArrayList<MediaFile>>>()
    val deleteImagesResponse = MutableLiveData<ResponseHandler<String>>()

    fun getImageExtracted(folderPath: String?) = viewModelScope.launch(Dispatchers.IO) {
        imagesResponse.postValue(ResponseHandler.Loading)
        if (folderPath == null) {
            imagesResponse.value = ResponseHandler.Failure(extra = "Cant get image!")
            return@launch
        }
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
            imagesResponse.postValue(ResponseHandler.Failure(ex, ex.message))
        }
    }

    fun deleteImage(folderPath: String?) = viewModelScope.launch(Dispatchers.IO) {
        deleteImagesResponse.postValue(ResponseHandler.Loading)
        if (folderPath == null) {
            deleteImagesResponse.value = ResponseHandler.Failure(extra = "Can't delete image!")
            return@launch
        }
        var flag = false
        try {
            deleteDir(folderPath)
            flag = !File(folderPath).exists()
        } catch (ex: Exception) {
            deleteImagesResponse.postValue(ResponseHandler.Failure(ex, ex.message))
        }
        if (flag) {
            deleteImagesResponse.postValue(ResponseHandler.Success("success"))
        } else {
            deleteImagesResponse.postValue(ResponseHandler.Failure(null, "delete failed"))
        }
    }
}