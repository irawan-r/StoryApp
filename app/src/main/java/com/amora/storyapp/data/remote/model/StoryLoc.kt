package com.amora.storyapp.data.remote.model

import com.squareup.moshi.Json

data class StoryLocResponse(

	@Json(name="listStory")
	val listStory: List<StoryItem>? = null,

	@Json(name="error")
	val error: Boolean? = null,

	@Json(name="message")
	val message: String? = null
)
