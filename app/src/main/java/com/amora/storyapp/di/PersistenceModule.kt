package com.amora.storyapp.di

import android.app.Application
import androidx.room.Room
import com.amora.storyapp.R
import com.amora.storyapp.data.persistence.AppDatabase
import com.amora.storyapp.data.persistence.RemoteKeysDao
import com.amora.storyapp.data.persistence.StoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

	@Provides
	@Singleton
	fun provideAppDatabase(application: Application): AppDatabase {
		return Room.databaseBuilder(
			application,
			AppDatabase::class.java,
			application.getString(R.string.database)
		).fallbackToDestructiveMigration()
			.build()
	}

	@Provides
	@Singleton
	fun providePosterDao(appDatabase: AppDatabase): StoryDao {
		return appDatabase.storyDao()
	}
}