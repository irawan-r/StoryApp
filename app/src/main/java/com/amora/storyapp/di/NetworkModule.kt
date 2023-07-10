package com.amora.storyapp.di

import com.amora.storyapp.BuildConfig
import com.amora.storyapp.network.ApiService
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

	@Provides
	fun provideOkHttpClient(): OkHttpClient {
		val loggingInterceptor = when {
			BuildConfig.DEBUG -> HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
			else -> HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
		}

		return OkHttpClient.Builder()
			.addInterceptor(loggingInterceptor)
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(10, TimeUnit.SECONDS)
			.build()
	}

	@Provides
	@Singleton
	fun provideApiServices(client: OkHttpClient): ApiService {
		return Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
			.baseUrl(BuildConfig.BASE_URL)
			.client(client)
			.build()
			.create(ApiService::class.java)
	}
}