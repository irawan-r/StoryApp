package com.amora.storyapp.ui.dashboard

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import androidx.paging.map
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.utils.DataDummy
import com.amora.storyapp.utils.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

	@get:Rule
	val instantExecutorRule = InstantTaskExecutorRule()

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

	@Test
	fun `when The Size Story As Expected`() = runBlocking {
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
		val listActualData = mutableListOf<StoryItem>()
		actualPagingData.collectLatest { actualData ->
			if ((actualData is State.Empty).not() && (actualData is State.Loading).not()) {
				assertTrue(actualData is State.Success)
				if (actualData.data != null) {
					actualData.data?.map { data ->
						listActualData.add(data)
					}
				}
			}
			assertEquals(dummyStories.size, listActualData.size)
		}
	}

	@Test
	fun `when The First Story As Expected`() = runBlocking {

		val postState = MutableStateFlow<State<NormalResponse>>(State.Empty())
		postState.value = State.Success(NormalResponse(false, "Sukses"))

		val doPost = repository.postStory(mContext, token, story, {}, {})
		doPost.collectLatest {
			// do nothing
		}
		val newStoryAdded = DataDummy.doAddNewStory()
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
		val listActualData = mutableListOf<StoryItem>()
		actualPagingData.collectLatest { actualData ->
			if ((actualData is State.Empty).not() && (actualData is State.Loading).not()) {
				assertTrue(actualData is State.Success)
				if (actualData.data != null) {
					actualData.data?.map { data ->
						listActualData.add(data)
					}
				}
				val newDummyStory = newStoryAdded.first()
				val actualStory = listActualData.first()
				val isReallyTheSameStory =
					newDummyStory.description.equals(actualStory.description) &&
							newDummyStory.lat!! == actualStory.lat &&
							newDummyStory.lon!! == actualStory.lon
				assert(isReallyTheSameStory)
			}
		}
	}

	@Test
	fun `when The Returned Story Was Empty`() = runBlocking {
		val dummyPagingData = PagingData.empty<StoryItem>()
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
		val listActualData = mutableListOf<StoryItem>()
		actualPagingData.collectLatest { actualData ->
			if ((actualData is State.Empty).not() && (actualData is State.Loading).not()) {
				assertTrue(actualData is State.Success)
				if (actualData.data != null) {
					actualData.data?.map { data ->
						listActualData.add(data)
					}
				}
			}
		}

		while (listActualData.isNotEmpty()) {

		}
	}
}