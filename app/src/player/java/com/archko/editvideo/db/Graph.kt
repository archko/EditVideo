package com.archko.editvideo.db

import android.content.Context
import androidx.room.Room

object Graph {
    lateinit var database: AKDatabase
        private set

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, AKDatabase::class.java, "tv_video_progress.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}