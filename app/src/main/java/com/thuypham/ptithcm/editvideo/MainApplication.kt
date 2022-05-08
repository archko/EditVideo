package com.thuypham.ptithcm.editvideo

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.thuypham.ptithcm.editvideo.di.repositoryModule
import com.thuypham.ptithcm.editvideo.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: MainApplication
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(applicationContext)
            modules(
                repositoryModule,
                viewModelModule,
            )
        }
    }

}