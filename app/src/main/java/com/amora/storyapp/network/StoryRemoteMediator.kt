package com.amora.storyapp.network

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.amora.storyapp.data.persistence.AppDatabase
import com.amora.storyapp.data.remote.model.RemoteKeys
import com.amora.storyapp.data.remote.model.StoryItem
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator @Inject constructor(
	private val database: AppDatabase,
	private val apiService: ApiService,
	private val token: String,
	private val location: Double
): RemoteMediator<Int, StoryItem>() {
	private companion object {
		const val INITIAL_PAGE_INDEX = 1
	}
	override suspend fun initialize(): InitializeAction {
		return InitializeAction.LAUNCH_INITIAL_REFRESH
	}

	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, StoryItem>
	): MediatorResult {
		val page = when (loadType) {
			LoadType.REFRESH ->{
				val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
				remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
			}
			LoadType.PREPEND -> {
				val remoteKeys = getRemoteKeyForFirstItem(state)
				val prevKey = remoteKeys?.prevKey
					?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
				prevKey
			}
			LoadType.APPEND -> {
				val remoteKeys = getRemoteKeyForLastItem(state)
				val nextKey = remoteKeys?.nextKey
					?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
				nextKey
			}
		}
		try {
			val responseData = apiService.getPagingStories(token, page, state.config.pageSize, location)
			val data = responseData.listStory
			val endOfPaginationReached = data.isNullOrEmpty()
			database.withTransaction {
				if (loadType == LoadType.REFRESH) {
					database.storyDao().deleteStoryList()
					database.remoteKeysDao().deleteRemoteKeys()
				}
				val prevKey = if (page == 1) null else page - 1
				val nextKey = if (endOfPaginationReached) null else page + 1
				val keys = responseData.listStory?.map {
					RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
				}
				if (keys != null) {
					database.remoteKeysDao().insertAll(keys)
				}
				if (data != null) {
					database.storyDao().insertStoryList(data)
				}
			}
			return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
		} catch (exception: Exception) {
			return MediatorResult.Error(exception)
		}
	}

	private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryItem>): RemoteKeys? {
		return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
			database.remoteKeysDao().getRemoteKeysId(data.id)
		}
	}
	private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryItem>): RemoteKeys? {
		return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
			database.remoteKeysDao().getRemoteKeysId(data.id)
		}
	}
	private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryItem>): RemoteKeys? {
		return state.anchorPosition?.let { position ->
			state.closestItemToPosition(position)?.id?.let { id ->
				database.remoteKeysDao().getRemoteKeysId(id)
			}
		}
	}
}