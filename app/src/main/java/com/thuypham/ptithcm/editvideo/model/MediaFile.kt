package com.thuypham.ptithcm.editvideo.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class MediaFile(
    @SerializedName("id")
    var id: Long,

    @field:SerializedName("path")
    var path: String? = null,

    @field:SerializedName("date_added")
    var dateAdded: Long? = null,

    @field:SerializedName("duration")
    var duration: Long? = null,

    @field:SerializedName("thumbnail")
    var thumbnail: String? = null,

    @field:SerializedName("bucket_name")
    var bucketName: String? = null,

    @field:SerializedName("media_type")
    var mediaType: Int,

    @field:SerializedName("size")
    var size: Long? = null,

    @field:SerializedName("display_name")
    var displayName: String? = null,

    @field:SerializedName("date_string")
    var dateString: String? = null,

    @field:SerializedName("uri")
    var uri: Uri? = null,

    var isSelected: Boolean? = false,

    ) {
    constructor() : this(0, "", 0, 0, "", "", 0)

    companion object {
        const val MEDIA_TYPE_IMAGE = 1
        const val MEDIA_TYPE_VIDEO = 2
        const val MEDIA_TYPE_AUDIO = 3
    }

    var isVideo = mediaType == MEDIA_TYPE_VIDEO

    var isImage = mediaType == MEDIA_TYPE_IMAGE

    var isAudio = mediaType == MEDIA_TYPE_AUDIO
    override fun toString(): String {
        return "MediaFile(path=$path, duration=$duration, displayName=$displayName, dateString=$dateString, uri=$uri)"
    }


}

