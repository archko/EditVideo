package com.archko.editvideo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProgress(progress: VideoProgress): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProgresses(progress: List<VideoProgress>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProgress(progress: VideoProgress)

    @Query("SELECT * FROM progress WHERE path = :path and uname = :uname")
    fun getProgress(path: String, uname: String): VideoProgress?

    @Query("SELECT * FROM progress order by record_last_timestamp desc")
    fun getAllProgress(): List<VideoProgress>?

    @Query("SELECT * FROM progress where uname = :uname order by record_last_timestamp desc limit :start, :count")
    fun getProgresses(start: Int, count: Int, uname: String): List<VideoProgress>?

    @Query("SELECT count(_id) FROM progress")
    fun progressCount(): Int

    //@Delete
    @Query("Delete FROM progress where path = :path and uname = :uname")
    fun deleteProgress(path: String, uname: String)

    //@Delete
    @Query("Delete FROM progress")
    fun deleteAllProgress()

    @Delete
    fun deleteProgress(progress: VideoProgress)
}