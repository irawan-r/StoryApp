package com.amora.storyapp.di

import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.local.SessionManager
import com.amora.storyapp.data.persistence.AppDatabase
import com.amora.storyapp.network.ApiService
import com.amora.storyapp.utils.FakeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainRepositoryModule {
	@Provides
	@Singleton
	fun provideMainRepository(
		sessionManager: SessionManager,
		apiService: ApiService,
		appDatabase: AppDatabase
	): MainRepository {
		return MainRepository(sessionManager, apiService, appDatabase)
	}
}