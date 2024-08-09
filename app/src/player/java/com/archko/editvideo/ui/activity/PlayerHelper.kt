package com.archko.editvideo.ui.activity

import android.text.TextUtils
import androidx.media3.exoplayer.ExoPlayer
import com.archko.editvideo.AppExecutors
import com.archko.editvideo.db.Graph
import com.archko.editvideo.db.VideoProgress
import timber.log.Timber

/**
 * @author: archko 2023/7/21 :09:15
 */
class PlayerHelper {

    companion object {

        //存储记录的最小时长.
        private val MIN_DURATION = 5L
        private val MIN_POSITION = 5L
        private val uname = "U1000"

        fun storeHistory(url: String?, duration: Long, position: Long) {
            Timber.d("storeHistory:$duration, $position, $url")
            if (TextUtils.isEmpty(url) || duration <= 0) {
                Timber.d("未获取到相关的进度信息:$url")
                return
            }

            var storePosition = position
            if (duration <= MIN_DURATION) {
                storePosition = 0
            }
            if (duration - storePosition <= MIN_POSITION) {
                storePosition = 0
            }

            if (!TextUtils.isEmpty(url)) {
                AppExecutors.instance.diskIO().execute {
                    if (duration > MIN_DURATION) {
                        var progress = Graph.database.progressDao()
                            .getProgress(url!!, uname)
                        if (null == progress) {
                            progress = VideoProgress(
                                url, uname,
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                1,
                                duration,
                                storePosition
                            )
                            Graph.database.progressDao().addProgress(progress)
                        } else {
                            progress.currentPosition = storePosition
                            Graph.database.progressDao().updateProgress(progress)
                        }
                    } else {
                        Graph.database.progressDao().deleteProgress(url!!, uname)
                    }
                }
            }
        }

        fun seekTo(mExoPlayer: ExoPlayer, url: String?) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            AppExecutors.instance.diskIO().execute {
                val progress = Graph.database.progressDao().getProgress(url!!, uname)
                progress?.run {
                    val startPosition = this.currentPosition * 1000
                    if (startPosition > 0) {
                        AppExecutors.instance.mainThread()
                            .execute { mExoPlayer.seekTo(startPosition) }
                    }
                }
            }
        }
    }
}