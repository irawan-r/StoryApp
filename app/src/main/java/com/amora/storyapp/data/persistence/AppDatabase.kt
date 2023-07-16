package com.amora.storyapp.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amora.storyapp.data.remote.model.RemoteKeys
import com.amora.storyapp.data.remote.model.StoryItem

@Database(entities = [StoryItem::class, RemoteKeys::class], version = 2, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {

	abstract fun storyDao(): StoryDao

	abstract fun remoteKeysDao(): RemoteKeysDao
}