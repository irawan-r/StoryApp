package com.amora.storyapp.di

import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.local.MainRepositoryImpl
import com.amora.storyapp.data.local.SessionManager
import com.amora.storyapp.data.persistence.AppDatabase
import com.amora.storyapp.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

	@Provides
	@Singleton
	fun provideRepository(
		sessionManager: SessionManager,
		apiService: ApiService,
		appDatabase: AppDatabase
	): MainRepository {
		// Provide an instance of the RepositoryMainImpl
		return MainRepositoryImpl(
			sessionManager = sessionManager,
			apiService = apiService,
			appDatabase = appDatabase
		)
	}
}