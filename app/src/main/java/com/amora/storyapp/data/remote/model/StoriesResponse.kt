package com.amora.storyapp.data.remote.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

data class StoriesResponse(

	@Json(name="listStory")
	val listStory: List<StoryItem>? = null,

	@Json(name="error")
	val error: Boolean? = null,

	@Json(name="message")
	val message: String? = null
)

@Entity(tableName = "story")
data class StoryItem(

	@Json(name="photoUrl")
	val photoUrl: String? = null,

	@Json(name="createdAt")
	val createdAt: String? = null,

	@Json(name="name")
	val name: String? = null,

	@Json(name="description")
	val description: String? = null,

	@Json(name="lon")
	val lon: Double? = null,

	@PrimaryKey
	@Json(name="id")
	val id: String,

	@Json(name="lat")
	val lat: Double? = null
)
