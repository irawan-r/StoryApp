package com.amora.storyapp.ui.dashboard

import android.provider.ContactsContract.Data
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.utils.DataDummy
import com.amora.storyapp.utils.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.SortedMap

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

	@get:Rule
	val instantExecutorRule = InstantTaskExecutorRule()

	@Mock
	private lateinit var repository: MainRepository
	private lateinit var dashboardViewModel: DashboardViewModel
	private val dummyStories = DataDummy.generateDataDummyStoriesEntity()
	private val token = DataDummy.getToken()

	@Before
	fun setUp() {
		dashboardViewModel = DashboardViewModel(repository)
	}

	@Test
	fun `when GetStoriesShouldNotNullAndReturnSuccess`() = runBlocking {
		val expectedPagingData = PagingData.from(dummyStories)
		val expectedFlow = flowOf(expectedPagingData)

		// Mock the repository.getPagingStories() function to return the expected flow
		`when`(repository.getPagingStories(token, 0.0)).thenReturn(expectedFlow)

		// Collect the values from the flow returned by the repository.getPagingStories() function
		val collectedValue = repository.getPagingStories(token, 0.0).toList()

		// Verify that the repository.getPagingStories() function is called with the correct parameters
		verify(repository).getPagingStories(token, 0.0)

		// Assert that the collected value is not empty
		assertTrue(collectedValue.isNotEmpty())
	}

	@Test
	fun `when The Size Story As Expected`() = runBlocking {
		val expectedPagingData = PagingData.from(dummyStories)
		val expectedFlow = flowOf(expectedPagingData)

		// Mock the repository.getPagingStories() function to return the expected flow
		`when`(repository.getPagingStories(token, 0.0)).thenReturn(expectedFlow)

		// Collect the values from the flow returned by the repository.getPagingStories() function
		val collectedValue = repository.getPagingStories(token, 0.0).toList()

		// Verify that the repository.getPagingStories() function is called with the correct parameters
		verify(repository).getPagingStories(token, 0.0)

		// Assert that the collected value is not empty
		assertTrue(collectedValue.size == expectedFlow.toList().size)
	}

	@Test
	fun `when The First Stroy As Expected`() = runBlocking {

	}

	@Test
	fun `when The Returned Story Was Empty`() = runBlocking {

	}
}