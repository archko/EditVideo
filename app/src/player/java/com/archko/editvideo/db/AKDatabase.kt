package com.archko.editvideo.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoProgress::class,
    ],
    version = 1,
    exportSchema = false
)
//@TypeConverters(DateTimeTypeConverters::class)
abstract class AKDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
}
