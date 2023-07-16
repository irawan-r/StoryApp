package com.amora.storyapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.StoryItem
import com.amora.storyapp.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _dashboardState = MutableStateFlow<State<PagingData<StoryItem>>>(State.Empty())
	val dashboardState = _dashboardState.asStateFlow()

	fun resetState() {
		_dashboardState.update {
			State.Empty()
		}
	}

	fun deleteSession() {
		repository.deleteSession()
	}


	fun getStories() {
		viewModelScope.launch {
			repository.getSession().collectLatest { userSession ->
				val token = userSession?.token
				if (token != null) {
					repository
						.getPagingStories(token, 0.0).cachedIn(viewModelScope)
						.onStart {
							_dashboardState.update { State.Loading() }
						}.onEmpty {
							_dashboardState.update { State.Empty() }
						}
						.collect { response ->
							_dashboardState.update {
								State.Success(response)
							}
						}
				}
			}

		}
	}
}