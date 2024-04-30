package io.flutter.plugins.exoplayer

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.archko.editvideo.ui.activity.FfmpegRenderersFactory
import org.chromium.net.CronetEngine
import timber.log.Timber
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors

/**
 * @author: archko 2023/7/3 :17:25
 */
@UnstableApi
class ExoSourceFactory {
    companion object {

        //字幕索引,一直增加,应该没有影片字幕有200个吧
        @JvmStatic
        private var subtitleId: Int = 200
        private const val USE_CRONET_FOR_NETWORKING = true

        private var httpDataSourceFactory: DataSource.Factory? = null

        //===============

        fun createMediaItem(context: Context, uri: Uri): MediaItem {
            val configurations = mutableListOf<MediaItem.SubtitleConfiguration>()
            /*if (!subtitlePaths.isNullOrEmpty()) {
                var index = 1
                for (subtitlePath in subtitlePaths) {
                    val configuration =
                        createSubtitleCfg(context, subtitlePath, subtitleId.toString(), index)
                    if (null != configuration) {
                        subtitleId++
                        index++
                        configurations.add(configuration)
                    }
                }
            }*/
            val builder = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(MediaMetadata.Builder().build())
            if (configurations.size > 0) {
                builder.setSubtitleConfigurations(configurations.toMutableList())
            }
            return builder.build()
        }

        fun createMediaItem(
            url: String?,
            context: Context
        ): MediaItem? {
            Timber.d("播放地址:$url")
            if (TextUtils.isEmpty(url)) {
                return null
            }

            return createMediaItem(
                context,
                Uri.parse(url),
            )
        }

        /*private fun createSubtitleCfg(
            context: Context,
            subtitlePath: String?,
            id: String,
            index: Int
        ): MediaItem.SubtitleConfiguration? {
            if (TextUtils.isEmpty(subtitlePath)) {
                return null
            }
            Timber.d("createSubtitleCfg.id:$id, index:$index, path:$subtitlePath")
            val mimeType: String? = MediaUtil.inferMimeType(subtitlePath)
            return MediaItem.SubtitleConfiguration
                .Builder(Uri.fromFile(File(subtitlePath!!)))
                .setLabel(
                    String.format(
                        context.getString(R.string.player_track_select_subtitle),
                        index
                    )
                )
                .setId(id)
                .setMimeType(mimeType)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }*/

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
                if (USE_CRONET_FOR_NETWORKING) {
                    val cronetEngine: CronetEngine? = CronetUtil.buildCronetEngine(context)
                    if (cronetEngine != null) {
                        httpDataSourceFactory =
                            CronetDataSource.Factory(
                                cronetEngine,
                                Executors.newSingleThreadExecutor()
                            );
                    }
                }
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
            val builder = ExoPlayer.Builder(context,
                FfmpegRenderersFactory(context)
            )
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