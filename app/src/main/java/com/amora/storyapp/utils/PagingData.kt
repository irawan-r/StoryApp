//package com.amora.storyapp.utils
//
//import androidx.lifecycle.viewModelScope
//import androidx.paging.Pager
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import androidx.paging.cachedIn
//import com.amora.storyapp.data.local.MainRepository
//import com.amora.storyapp.data.remote.model.StoryItem
//import kotlinx.coroutines.flow.EmptyFlow.collect
//import kotlinx.coroutines.flow.collect
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.onEmpty
//import kotlinx.coroutines.flow.onStart
//import kotlinx.coroutines.flow.update
//import javax.inject.Inject
//
//class PagingData @Inject constructor(
//	private val repository: MainRepository
//) : PagingSource<Int, StoryItem>() {
//	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
//		return try {
//			val pageNumber = params.key ?: 1
//			val pageSize = params.loadSize
//			// Make a network request or retrieve data from a local database
//			// based on the pageNumber and pageSize parameters
//			// and return the data as LoadResult.Page\
//			repository.getSession().collectLatest { userSession ->
//				val token = userSession?.token
//				if (token != null) {
//					repository.getStories(token, 1, 5, 0.0,
//						onSuccess = { response ->
//
//						}, onError = { error ->
//
//						}
//					).collect()
//				}
//			}
//			LoadResult.Page(
//				data = data,
//				prevKey = if (pageNumber > 1) pageNumber - 1 else null,
//				nextKey = if (data.isNotEmpty()) pageNumber + 1 else null
//			)
//		} catch (e: Exception) {
//			LoadResult.Error(e)
//		}
//	}
//
//	override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
//		TODO("Not yet implemented")
//	}
//}
