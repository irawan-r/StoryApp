package com.amora.storyapp.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.amora.storyapp.data.local.MainRepositoryImpl
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.utils.DataDummy
import com.amora.storyapp.utils.FakeStoryPagingSource
import com.amora.storyapp.utils.MainDispatcherRule
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

	@get:Rule
	val instantExecutorRule = InstantTaskExecutorRule()

	@OptIn(ExperimentalCoroutinesApi::class)
	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	@Mock
	private lateinit var repository: MainRepositoryImpl

	private lateinit var dashboardViewModel: DashboardViewModel
	private val dummyStories = DataDummy.generateDataDummyStoriesEntity()
	private val emptyStories = DataDummy.generateEmptyStoriesEntity()

	@Before
	fun setUp() {
		dashboardViewModel = DashboardViewModel(repository)
	}

	@Test
	fun `when GetStories Should Not Null And Return Success`() = runBlocking {
		val data: PagingData<StoryItem> = FakeStoryPagingSource.snapshot(dummyStories)

		val expectedPagingData = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())
		expectedPagingData.update {
			State.Success(data)
		}

		val expectedFlow = flowOf(data)
		val pagingStory = repository.getPagingStories(1, 0.0)
		`when`(pagingStory).thenReturn(expectedFlow)

		val differ = AsyncPagingDataDiffer(
			diffCallback = AdapterStory.differCallback,
			updateCallback = Utils.noopListUpdateCallback,
			workerDispatcher = Dispatchers.Main
		)

		dashboardViewModel.getStories()
		verify(repository).getPagingStories(1, 0.0)

		val actualData = dashboardViewModel.dashboardState.first()
		differ.submitData(actualData.data!!)

		assertTrue(actualData is State.Success)
		assertNotNull(differ.snapshot())
		assertEquals(dummyStories.size, differ.snapshot().size)
		assertEquals(dummyStories[0], differ.snapshot()[0])
	}

	@Test
	fun `when GetStories Is Empty`() = runBlocking {
		val data: PagingData<StoryItem> = PagingData.empty()

		val expectedPagingData = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())
		expectedPagingData.update {
			State.Success(data)
		}

		val expectedFlow = flowOf(data)
		val pagingStory = repository.getPagingStories(1, 0.0)
		`when`(pagingStory).thenReturn(expectedFlow)

		val differ = AsyncPagingDataDiffer(
			diffCallback = AdapterStory.differCallback,
			updateCallback = Utils.noopListUpdateCallback,
			workerDispatcher = Dispatchers.Main
		)

		dashboardViewModel.getStories()
		verify(repository).getPagingStories(1, 0.0)

		val actualData = dashboardViewModel.dashboardState.first()
		differ.submitData(actualData.data!!)

		assertTrue(actualData is State.Success)
		assertNotNull(differ.snapshot())
		assertEquals(emptyStories.size, differ.snapshot().size)
	}
}