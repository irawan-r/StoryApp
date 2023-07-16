package com.amora.storyapp.network

import com.amora.storyapp.data.remote.model.LoginResponse
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.StoriesResponse
import com.amora.storyapp.data.remote.model.StoryLocResponse
import com.amora.storyapp.data.remote.model.StoryResponse
import com.skydoves.sandwich.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

private const val AUTH = "Authorization"

interface ApiService {
	@POST("register")
	@FormUrlEncoded
	suspend fun register(
		@Field("name") name: String,
		@Field("email") email: String,
		@Field("password") password: String
	): ApiResponse<NormalResponse>

	@POST("login")
	@FormUrlEncoded
	suspend fun login(
		@Field("email") email: String,
		@Field("password") password: String
	): ApiResponse<LoginResponse>

	@POST("stories")
	@Multipart
	suspend fun postStories(
		@Header(AUTH) token: String,
		@Part("description") desc: RequestBody,
		@Part filePhoto: MultipartBody.Part,
		@Part("lat") latitude: RequestBody,
		@Part("lon") longitude: RequestBody
	): ApiResponse<NormalResponse>

	@Multipart
	@POST("stories/guest")
	suspend fun postStoriesGuest(
		@Header(AUTH) token: String,
		@Part("description") description: RequestBody,
		@Part photo: MultipartBody.Part,
		@Part("lat") lat: RequestBody?,
		@Part("lon") lon: RequestBody?
	): ApiResponse<NormalResponse>

	@GET("stories")
	suspend fun getAllStories(
		@Header(AUTH) token: String,
		@Query("page") page: Int?,
		@Query("size") size: Int?,
		@Query("location") location: Double?
	): ApiResponse<StoriesResponse>

	@GET("stories")
	suspend fun getPagingStories(
		@Header(AUTH) token: String,
		@Query("page") page: Int?,
		@Query("size") size: Int?,
		@Query("location") location: Double?
	): StoriesResponse

	@GET("stories/{id}")
	suspend fun getStoriesById(
		@Header(AUTH) token: String,
		@Path("id") id: String
	): ApiResponse<StoryResponse>

	@GET("stories")
	suspend fun getFriendsLocation(
		@Header(AUTH) token: String,
		@Query("location") location: Double = 1.0
	): ApiResponse<StoryLocResponse>
}