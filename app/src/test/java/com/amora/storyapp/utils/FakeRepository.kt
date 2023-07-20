package com.amora.storyapp.utils

import androidx.paging.PagingData
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.StoryItem
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeRepository {
	private val flow = MutableSharedFlow<PagingData<StoryItem>>()
	suspend fun emit(value: StoryItem) = flow.emit(PagingData.from(DataDummy.generateDataDummyStoriesEntity()))
}