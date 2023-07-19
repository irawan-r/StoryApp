package com.amora.storyapp.data.local

import android.content.Context
import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.amora.storyapp.data.persistence.AppDatabase
import com.amora.storyapp.data.remote.model.LoginRequest
import com.amora.storyapp.data.remote.model.LoginResponse
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.RegisterRequest
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.data.remote.model.StoryLocResponse
import com.amora.storyapp.data.remote.model.StoryRequest
import com.amora.storyapp.data.remote.model.StoryResponse
import com.amora.storyapp.data.remote.model.User
import com.amora.storyapp.network.ApiService
import com.amora.storyapp.network.StoryRemoteMediator
import com.amora.storyapp.utils.ApiUtils.toRequestBodyPart
import com.amora.storyapp.utils.Utils.compressImageToFile
import com.google.gson.Gson
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLDecoder
import javax.inject.Inject

class MainRepository @Inject constructor(
	private val sessionManager: SessionManager,
	private val apiService: ApiService,
	private val appDatabase: AppDatabase
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
		}.flowOn(Dispatchers.IO)
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
		}.onException {
			onError(this.message.toString())
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
		}.onException {
		onError(this.message.toString())
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
		val compressedFile = File(context.cacheDir, "compressed_${filePhoto.name}")
		val latitude = request.lat.toRequestBodyPart()
		val longitude = request.lon.toRequestBodyPart()
		val requestFile: RequestBody?
		var finalFileName = filePhoto.name
		if (!filePhoto.exists()) {
			val inputStream = context.contentResolver.openInputStream(photoUri?.toUri()!!)
			if (inputStream != null) {
				compressImageToFile(inputStream, compressedFile)
			}

			val compressedByteArray = compressedFile.readBytes()
			requestFile = compressedByteArray.toRequestBody("image/*".toMediaTypeOrNull())
			finalFileName = URLDecoder.decode(filePhoto.name, "UTF-8") + ".jpg"
		} else {
			compressImageToFile(filePhoto, compressedFile)
			requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
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
		}.onException {
			onError(this.message.toString())
		}
	}.flowOn(Dispatchers.IO)

	@OptIn(ExperimentalPagingApi::class)
	fun getPagingStories(
		page: Int? = 1,
		token: String,
		location: Double
	): Flow<PagingData<StoryItem>> {
		val bearerToken = "Bearer $token"
		return Pager(
			config = PagingConfig(
				pageSize = 10
			),
			remoteMediator = StoryRemoteMediator(appDatabase, apiService, bearerToken, location, page),
			pagingSourceFactory = {
				appDatabase.storyDao().getStoryList()
			}
		).flow
	}

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
		}.onException {
			onError(this.message.toString())
		}
	}.flowOn(Dispatchers.IO)

	fun getFriendLoc(
		token: String,
		onSuccess: (StoryLocResponse) -> Unit,
		onError: (String) -> Unit
	) = flow {
		val bearer = "Bearer $token"
		val getFriendLoc = apiService.getFriendsLocation(bearer)
		getFriendLoc.suspendOnSuccess {
			onSuccess(data)
			emit(data)
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
		}.onException {
			onError(this.message.toString())
		}
	}.flowOn(Dispatchers.IO)
}