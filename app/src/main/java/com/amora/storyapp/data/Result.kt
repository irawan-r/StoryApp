package com.amora.storyapp.data

sealed class Result<out T> {

	data class Success<out T>(val data: T): Result<T>()
	data class Error(val errorMsg: String): Result<Nothing>()
}