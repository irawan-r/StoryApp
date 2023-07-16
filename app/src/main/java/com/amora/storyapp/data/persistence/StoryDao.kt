package com.amora.storyapp.data.persistence

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amora.storyapp.data.remote.model.StoryItem

@Dao
interface StoryDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertStoryList(poster: List<StoryItem>)

	@Query("SELECT * FROM story WHERE id = :id_")
	suspend fun getStory(id_: Long): StoryItem?

	@Query("SELECT * FROM story")
	fun getStoryList(): PagingSource<Int, StoryItem>

	@Query("DELETE FROM story")
	suspend fun deleteStoryList()
}