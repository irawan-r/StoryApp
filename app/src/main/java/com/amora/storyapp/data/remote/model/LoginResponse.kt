package com.amora.storyapp.data.remote.model

import com.squareup.moshi.Json

data class LoginResponse(

	@Json(name="loginResult")
	val loginResult: User? = null,

	@Json(name="error")
	val error: Boolean? = null,

	@Json(name="message")
	val message: String? = null
)

data class User(

	@Json(name="name")
	val name: String? = null,

	@Json(name="userId")
	val userId: String? = null,

	@Json(name="token")
	val token: String? = null
)
