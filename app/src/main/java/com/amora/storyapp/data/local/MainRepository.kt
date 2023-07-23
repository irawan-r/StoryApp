package com.amora.storyapp.data.local

import android.content.Context
import androidx.paging.PagingData
import com.amora.storyapp.data.remote.model.LoginRequest
import com.amora.storyapp.data.remote.model.LoginResponse
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.RegisterRequest
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.data.remote.model.StoryLocResponse
import com.amora.storyapp.data.remote.model.StoryRequest
import com.amora.storyapp.data.remote.model.StoryResponse
import com.amora.storyapp.data.remote.model.User
import kotlinx.coroutines.flow.Flow

interface MainRepository {
	fun setSession(user: User)
	fun deleteSession()
	fun getSession(): Flow<User?>
	fun login(
		request: LoginRequest,
		onSuccess: (LoginResponse?) -> Unit,
		onError: (String) -> Unit
	): Flow<LoginResponse>
	fun register(
		register: RegisterRequest,
		onSuccess: (NormalResponse) -> Unit,
		onError: (String) -> Unit
	): Flow<NormalResponse>
	fun postStory(
		context: Context,
		token: String,
		request: StoryRequest,
		onSuccess: (NormalResponse) -> Unit,
		onError: (String) -> Unit
	): Flow<String?>
	suspend fun getPagingStories(
		page: Int? = 1,
		location: Double
	): Flow<PagingData<StoryItem>>
	fun getStory(
		token: String,
		id: String,
		onSuccess: (StoryResponse) -> Unit,
		onError: (String) -> Unit
	): Flow<String?>

	fun getFriendLoc(
		token: String,
		onSuccess: (StoryLocResponse) -> Unit,
		onError: (String) -> Unit
	): Flow<StoryLocResponse>

}