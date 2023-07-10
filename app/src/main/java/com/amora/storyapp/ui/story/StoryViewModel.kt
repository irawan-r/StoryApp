package com.amora.storyapp.ui.story

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.StoryRequest
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
class StoryViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _postResult = MutableStateFlow<State<NormalResponse>>(State.Empty())
	val postResult = _postResult.asStateFlow()

	fun resetState() {
		_postResult.update {
			State.Empty()
		}
	}

	fun sendIdeas(context: Context, story: StoryRequest) {
		viewModelScope.launch {
			when {
				story.description.isEmpty() -> {
					_postResult.update {
						State.Error("Isi dulu biar harimu berwarna :)")
					}

				}
				story.photo.isNullOrEmpty() -> {
					_postResult.update {
						State.Error("Wajib dengan Foto")
					}
				}
				else -> {
					repository.getSession().collectLatest { userSession ->
						val token = userSession?.token
						if (token != null) {
							repository.postStory(context, token,
								story, onSuccess = { response ->
									_postResult.update {
										State.Success(response)
									}
								}
							) { error ->
								_postResult.update {
									State.Error(error)
								}
							}.onStart {
								_postResult.update { State.Loading() }
							}.onEmpty {
								_postResult.update { State.Empty() }
							}.collect()
						}
					}
				}
			}
		}
	}
}