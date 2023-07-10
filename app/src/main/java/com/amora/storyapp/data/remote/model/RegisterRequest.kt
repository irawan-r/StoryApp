package com.amora.storyapp.data.remote.model

data class RegisterRequest(
	var name: String = "",
	var email: String = "",
	var password: String = ""
)