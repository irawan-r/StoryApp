package com.amora.storyapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.StoryLocResponse
import com.amora.storyapp.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _mapState = MutableStateFlow<State<StoryLocResponse>>(State.Empty())
	val mapState = _mapState.asStateFlow()

	fun resetState() {
		_mapState.update {
			State.Empty()
		}
	}

	init {
		getUsersLocation()
	}

	private fun getUsersLocation() {
		viewModelScope.launch {
			repository.getSession().collectLatest { userSession ->
				val token = userSession?.token
				if (token != null) {
					repository.getFriendLoc(token, onSuccess = { response ->
						_mapState.update { State.Success(response) }
					}, onError = {
						_mapState.update { State.Error("error") }
					}).onStart {
						_mapState.update { State.Loading() }
					}.collect()
				}
			}
		}
	}
}