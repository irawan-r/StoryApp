package com.amora.storyapp.data.remote.model


data class StoryRequest(
	var description: String = "",
	var photo: String? = "",
	var lat: Double? = 0.0,
	var lon: Double? = 0.0
)