package com.amora.storyapp.ui.dashboard

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.map
import androidx.recyclerview.widget.ListUpdateCallback
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.data.remote.model.User
import com.amora.storyapp.network.PagingDataSource
import com.amora.storyapp.utils.DataDummy
import com.amora.storyapp.utils.FakeRepository
import com.amora.storyapp.utils.MainDispatcherRule
import com.amora.storyapp.utils.State
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyDouble
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

	@get:Rule
	val instantExecutorRule = InstantTaskExecutorRule()

	@OptIn(ExperimentalCoroutinesApi::class)
	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	@Mock
	private lateinit var repository: MainRepository

	private lateinit var dashboardViewModel: DashboardViewModel
	private val dummyStories = DataDummy.generateDataDummyStoriesEntity()
	private val token = DataDummy.getToken()
	private val story = DataDummy.generateStoryData()
	private val mContext = mock(Context::class.java)


	@Before
	fun setUp() {
		dashboardViewModel = DashboardViewModel(repository)
	}

	@Test
	fun `when GetStoriesShouldNotNullAndReturnSuccess`() = runBlocking {
		val dummyPagingData = PagingData.from(dummyStories)
		val expectedPagingData = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())
		expectedPagingData.value = State.Loading()
		expectedPagingData.value = State.Success(dummyPagingData)
		val expectedFlow = flowOf(expectedPagingData.value.data!!)
		`when`(repository.getPagingStories(null, token, 0.0)).thenReturn(expectedFlow)

		val actualPagingData = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())

		verify(repository).getPagingStories(null, token, 0.0)
		repository.getPagingStories(null, token, 0.0).collectLatest { actualData ->
			actualPagingData.update {
				State.Loading()
			}
			actualPagingData.update {
				State.Success(actualData)
			}
		}

		actualPagingData.collectLatest { actualData ->
			if ((actualData is State.Empty).not() && (actualData is State.Loading).not()) {
				assertTrue(actualData is State.Success)
				assertNotNull(actualData.data)
				assertEquals(actualData.data, expectedPagingData.value.data)
			}
		}

	}

	private val noopListUpdateCallback = object : ListUpdateCallback {
		override fun onInserted(position: Int, count: Int) {}
		override fun onRemoved(position: Int, count: Int) {}
		override fun onMoved(fromPosition: Int, toPosition: Int) {}
		override fun onChanged(position: Int, count: Int, payload: Any?) {}
	}

	@Test
	fun `when The Size Story As Expected`(): Unit = runBlocking {
		val data: PagingData<StoryItem> = StoryPagingSource.snapshot(dummyStories)

		val expectedPagingData = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())
		expectedPagingData.update {
			State.Success(data)
		}

		val expectedFlow = flowOf(data)
		val pagingStory = repository.getPagingStories(any(), anyString(), anyDouble())
		`when`(pagingStory).thenReturn(expectedFlow)

		val differ = AsyncPagingDataDiffer(
			diffCallback = AdapterStory.differCallback,
			updateCallback = noopListUpdateCallback,
			workerDispatcher = Dispatchers.Default
		)

		dashboardViewModel.getStories()
		dashboardViewModel.dashboardState.collectLatest {
			differ.submitData(it.data!!)
		}

		verify(repository).getPagingStories(null, token, 0.0)

		assertNotNull(differ.snapshot())
		assertEquals(dummyStories.size, differ.snapshot().size)
		assertEquals(dummyStories[0], differ.snapshot()[0])
	}


	class StoryPagingSource : PagingSource<Int, Flow<List<StoryItem>>>() {
		companion object {
			fun snapshot(items: List<StoryItem>): PagingData<StoryItem> {
				return PagingData.from(items)
			}
		}
		override fun getRefreshKey(state: PagingState<Int, Flow<List<StoryItem>>>): Int {
			return 0
		}
		override suspend fun load(params: LoadParams<Int>): PagingSource.LoadResult<Int, Flow<List<StoryItem>>> {
			return PagingSource.LoadResult.Page(emptyList(), 0, 1)
		}
	}

	@Test
	fun `when The First Story As Expected`() = runBlocking {

	}

	@Test
	fun `when The Returned Story Was Empty`() = runBlocking {

	}
}