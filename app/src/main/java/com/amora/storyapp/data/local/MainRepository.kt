package com.amora.storyapp.data.local

import android.content.Context
import androidx.core.net.toUri
import com.amora.storyapp.data.remote.model.LoginRequest
import com.amora.storyapp.data.remote.model.LoginResponse
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.RegisterRequest
import com.amora.storyapp.data.remote.model.StoriesResponse
import com.amora.storyapp.data.remote.model.StoryRequest
import com.amora.storyapp.data.remote.model.StoryResponse
import com.amora.storyapp.data.remote.model.User
import com.amora.storyapp.network.ApiService
import com.amora.storyapp.utils.ApiUtils.toRequestBodyPart
import com.google.gson.Gson
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLDecoder
import javax.inject.Inject

class MainRepository @Inject constructor(
	private val sessionManager: SessionManager,
	private val apiService: ApiService
) {
	private fun setSession(user: User) {
		sessionManager.loginSession(user)
	}

	fun deleteSession() {
		sessionManager.deleteSession()
	}

	fun getSession(): Flow<User?> {
		return flow {
			sessionManager.getSession()?.let { emit(it) }
		}.flowOn(Dispatchers.Default)
	}

	fun login(
		request: LoginRequest,
		onSuccess: (LoginResponse?) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val loginRequest = apiService.login(request.email, request.password)
		loginRequest.suspendOnSuccess {
			data.loginResult?.let { setSession(it) }
			emit(data)
			onSuccess(data)
		}.onError {
			val message: String? = try {
				val errorMessageObj = Gson().fromJson(message(), LoginResponse::class.java)
				errorMessageObj.message?.replace("\"", "")
			} catch (e: Exception) {
				onError(e.message.toString())
				null
			}
			if (message != null) {
				onError(message)
			}
		}
	}.flowOn(Dispatchers.IO)

	fun register(
		register: RegisterRequest,
		onSuccess: (NormalResponse) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val name = register.name
		val email = register.email
		val pass = register.password
		val registerRequest = apiService.register(name, email, pass)
		registerRequest.suspendOnSuccess {
			emit(data)
			onSuccess(data)
		}.onError {
			val message: String? = try {
				val errorMessageObj = Gson().fromJson(message(), NormalResponse::class.java)
				errorMessageObj.message?.replace("\"", "")
			} catch (e: Exception) {
				onError(e.message.toString())
				null
			}
			if (message != null) {
				onError(message)
			}
		}
	}.flowOn(Dispatchers.IO)

	fun postStory(
		context: Context,
		token: String,
		request: StoryRequest,
		onSuccess: (NormalResponse) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val bearerToken = "Bearer $token"
		val description = request.description.toRequestBodyPart()
		val photoUri = request.photo
		val filePhoto = File(photoUri.toString())
		val latitude = request.lat.toRequestBodyPart()
		val longitude = request.lon.toRequestBodyPart()
		var requestFile = filePhoto.asRequestBody("image/*".toMediaTypeOrNull())
		var finalFileName = filePhoto.name
		if (!filePhoto.exists()) {
			val inputStream = context.contentResolver.openInputStream(photoUri?.toUri()!!)
			requestFile = inputStream?.use { input ->
				input.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
			}!!
			finalFileName = URLDecoder.decode(filePhoto.name, "UTF-8") + ".jpg"
		}
		val imgPhoto = MultipartBody.Part.createFormData("photo", finalFileName, requestFile)
		val postRequest = apiService.postStories(
			token = bearerToken,
			desc = description,
			filePhoto = imgPhoto,
			latitude = latitude,
			longitude = longitude
		)

		postRequest.suspendOnSuccess {
			onSuccess(data)
			emit(data.message)
		}.onError {
			val message: String? = try {
				val errorMessageObj = Gson().fromJson(message(), NormalResponse::class.java)
				errorMessageObj.message?.replace("\"", "")
			} catch (e: Exception) {
				onError(e.message.toString())
				null
			}
			if (message != null) {
				onError(message)
			}
		}.onError {
			onError(message())
		}.onException {
			onError(message())
		}
	}.flowOn(Dispatchers.IO)

	fun getStories(
		token: String,
		page: Int?,
		size: Int?,
		location: Double?,
		onSuccess: (StoriesResponse?) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val bearerToken = "Bearer $token"
		val getStories =
			apiService.getAllStories(token = bearerToken, page = page, size = size, location = location)
		getStories.suspendOnSuccess {
			onSuccess(data)
			emit(data.message)
		}.onError {
			val message: String? = try {
				val errorMessageObj = Gson().fromJson(message(), NormalResponse::class.java)
				errorMessageObj.message?.replace("\"", "")
			} catch (e: Exception) {
				onError(e.message.toString())
				null
			}
			if (message != null) {
				onError(message)
			}
		}
	}.flowOn(Dispatchers.IO)

	fun getStory(
		token: String,
		id: String,
		onSuccess: (StoryResponse) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val bearerToken = "Bearer $token"
		val getStory = apiService.getStoriesById(token = bearerToken, id = id)
		getStory.suspendOnSuccess {
			emit(data.message)
			onSuccess(data)
		}.onException {
			onError(message())
		}.onError {
			val message: String? = try {
				val errorMessageObj = Gson().fromJson(message(), NormalResponse::class.java)
				errorMessageObj.message?.replace("\"", "")
			} catch (e: Exception) {
				onError(e.message.toString())
				null
			}
			if (message != null) {
				onError(message)
			}
		}
	}.flowOn(Dispatchers.IO)
}