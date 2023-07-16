package com.amora.storyapp.network

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.StoryItem
import com.google.gson.Gson
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PagingDataSource @Inject constructor(
	private val apiService: ApiService,
	private val token: String,
	private val location: Double?
) : PagingSource<Int, StoryItem>() {

	private companion object {
		const val INITIAL_PAGE_INDEX = 1
	}

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
		return try {
			val position = params.key ?: INITIAL_PAGE_INDEX
			val pageSize = params.loadSize

			var data: List<StoryItem> = emptyList()
			val response = apiService.getAllStories(token, position, pageSize, location)
			var message: String
			response.suspendOnSuccess {
				data = this.data.listStory ?: emptyList()
			}.onError {
				message = try {
					val errorMessageObj = Gson().fromJson(this.message(), NormalResponse::class.java)
					errorMessageObj.message?.replace("\"", "").toString()
				} catch (e: Exception) {
					e.message.toString()
				}
			}

			val nextPage = if (data.isEmpty()) null else position + 1

			LoadResult.Page(
				data = data,
				prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
				nextKey = nextPage
			)
		} catch (exception: IOException) {
			LoadResult.Error(exception)
		} catch (exception: HttpException) {
			LoadResult.Error(exception)
		}
	}

	override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
		return state.anchorPosition?.let { anchorPosition ->
			val anchorPage = state.closestPageToPosition(anchorPosition)
			anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
		}
	}
}