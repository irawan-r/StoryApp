package com.amora.storyapp.ui.storydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.StoryResponse
import com.amora.storyapp.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailStoryViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _detailStoryState = MutableStateFlow<State<StoryResponse>>(State.Empty())
	val detailStory = _detailStoryState.asStateFlow()

	fun resetState() {
		_detailStoryState.update {
			State.Empty()
		}
	}

	fun getStory(id: String) {
		viewModelScope.launch {
			repository.getSession().collectLatest { userSession ->
				val token = userSession?.token
				if (token != null) {
					repository.getStory(token, id, onSuccess = { response ->
						_detailStoryState.update {
							State.Success(response)
						}
					}, onError = { error ->
						_detailStoryState.update {
							State.Error(error)
						}
					}).onStart {
						_detailStoryState.update { State.Loading() }
					}.onEmpty {
						_detailStoryState.update { State.Empty() }
					}.collect()
				}
			}
		}

	}
}