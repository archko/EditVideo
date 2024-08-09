package com.archko.editvideo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @author: archko 2023/8/17 :16:27
 */
@Entity(
    tableName = "progress",
)
class VideoProgress : Serializable, Comparator<VideoProgress> {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Int = 0

    /**
     * 文件路径,不是全路径
     */
    @JvmField
    @ColumnInfo(name = "path")
    var path: String? = null

    /**
     * 视频相关用户名
     */
    @JvmField
    @ColumnInfo(name = "uname")
    var uname: String? = null

    @JvmField
    @ColumnInfo(name = "size")
    var size: Long = 0

    @JvmField
    @ColumnInfo(name = "record_first_timestamp")
    var firstTimestampe: Long = 0

    @JvmField
    @ColumnInfo(name = "record_last_timestamp")
    var lastTimestampe: Long = 0

    @JvmField
    @ColumnInfo(name = "record_times")
    var readTimes: Int = 0

    /**
     * 视频时长,秒
     */
    @JvmField
    @ColumnInfo(name = "duration")
    var duration: Long = 0

    /**
     * 视频进度,秒
     */
    @JvmField
    @ColumnInfo(name = "current_position")
    var currentPosition: Long = 0

    @Ignore
    constructor() {
    }

    constructor(
        path: String?,
        uname: String?,
        firstTimestampe: Long,
        lastTimestampe: Long,
        readTimes: Int,
        duration: Long,
        currentPosition: Long
    ) {
        this.path = path
        this.uname = uname
        this.firstTimestampe = firstTimestampe
        this.lastTimestampe = lastTimestampe
        this.readTimes = readTimes
        this.duration = duration
        this.currentPosition = currentPosition
    }


    override fun compare(lhs: VideoProgress, rhs: VideoProgress): Int {
        if (lhs.lastTimestampe > rhs.lastTimestampe) {    //时间大的放前面
            return -1
        } else if (lhs.lastTimestampe < rhs.lastTimestampe) {
            return 1
        }
        return 0
    }

    override fun toString(): String {
        return "VideoProgress(_id=$_id, path=$path, uname=$uname, size=$size, firstTimestampe=$firstTimestampe, lastTimestampe=$lastTimestampe, readTimes=$readTimes, duration=$duration, currentPosition=$currentPosition)"
    }
}