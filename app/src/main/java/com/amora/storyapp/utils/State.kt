package com.amora.storyapp.utils

sealed class State<T>(val data: T? = null, val message: String? = null) {
	class Success<T>(data: T? = null): State<T>(data)
	class Loading<T>(data: T? = null): State<T>(data)
	class Error<T>(message: String? = null, data: T? = null): State<T>(data, message)
	class Empty<T>: State<T>()
}