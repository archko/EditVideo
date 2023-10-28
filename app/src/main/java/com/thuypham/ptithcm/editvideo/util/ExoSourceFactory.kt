package io.flutter.plugins.exoplayer

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.thuypham.ptithcm.editvideo.util.FfmpegRenderersFactory
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * @author: archko 2023/7/3 :17:25
 */
class ExoSourceFactory {
    companion object {

        //字幕索引,一直增加,应该没有影片字幕有200个吧
        @JvmStatic
        private var subtitleId: Int = 200
        private const val USE_CRONET_FOR_NETWORKING = true

        private var httpDataSourceFactory: DataSource.Factory? = null

        //===============

        private var dataSourceFactory: DataSource.Factory? = null
        private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

        private var downloadDirectory: File? = null
        private var downloadCache: Cache? = null

        private fun buildReadOnlyCacheDataSource(
            upstreamFactory: DataSource.Factory, cache: Cache
        ): CacheDataSource.Factory {
            return CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }

        @Synchronized
        private fun getDownloadDirectory(context: Context): File? {
            if (downloadDirectory == null) {
                downloadDirectory = context.getExternalFilesDir( /* type= */null)
                if (downloadDirectory == null) {
                    downloadDirectory = context.filesDir
                }
            }
            return downloadDirectory
        }

        @Synchronized
        private fun getDownloadCache(context: Context): Cache {
            if (downloadCache == null) {
                val downloadContentDirectory =
                    File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
                downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
            }
            return downloadCache!!
        }

        @Synchronized
        fun getHttpDataSourceFactory(context: Context): DataSource.Factory {
            if (httpDataSourceFactory == null) {
                if (httpDataSourceFactory == null) {
                    // We don't want to use Cronet, or we failed to instantiate a CronetEngine.
                    val cookieManager = CookieManager()
                    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
                    CookieHandler.setDefault(cookieManager)
                    httpDataSourceFactory = DefaultHttpDataSource.Factory()
                }
            }
            return httpDataSourceFactory!!
        }

        @Synchronized
        fun getDataSourceFactory(context: Context): DataSource.Factory? {
            if (dataSourceFactory == null) {
                val ctx = context.applicationContext
                val upstreamFactory = DefaultDataSource.Factory(ctx, getHttpDataSourceFactory(ctx))
                dataSourceFactory =
                    buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(ctx))
            }
            return dataSourceFactory
        }

        fun buildMediaSourceFactory(context: Context): MediaSource.Factory {
            return DefaultMediaSourceFactory(context)
                .setDataSourceFactory(getDataSourceFactory(context)!!)
        }

        fun buildPlayer(context: Context): ExoPlayer {
            val builder = ExoPlayer.Builder(context, FfmpegRenderersFactory(context))
                .setMediaSourceFactory(buildMediaSourceFactory(context))
            val trackSelector = DefaultTrackSelector(context)
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd()
                    .setPreferredTextLanguage(context.resources.configuration.locale.language)
            )
            return builder.setTrackSelector(trackSelector).build()
        }
    }
}