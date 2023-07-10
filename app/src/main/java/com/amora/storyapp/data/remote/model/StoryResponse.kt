package com.amora.storyapp.data.remote.model

import com.squareup.moshi.Json

data class StoryResponse(

	@Json(name="error")
	val error: Boolean? = null,

	@Json(name="message")
	val message: String? = null,

	@Json(name="story")
	val story: Story? = null
)

data class Story(

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

	@Json(name="id")
	val id: String? = null,

	@Json(name="lat")
	val lat: Double? = null
)
