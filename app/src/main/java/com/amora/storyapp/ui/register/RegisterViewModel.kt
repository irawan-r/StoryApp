package com.amora.storyapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.NormalResponse
import com.amora.storyapp.data.remote.model.RegisterRequest
import com.amora.storyapp.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _registerResult = MutableStateFlow<State<NormalResponse>>(State.Empty())
	val registerResult = _registerResult.asStateFlow()

	fun register(register: RegisterRequest) {
		when {
			register.name.isBlank() -> {
				_registerResult.update {
					State.Error("Nama tidak boleh kosong")
				}
			}

			register.email.isBlank() -> {
				_registerResult.update {
					State.Error("Email tidak boleh kosong")
				}
			}
			register.password.isBlank() -> {
				_registerResult.update {
					State.Error("Password tidak boleh kosong")
				}
			}
			else -> {
				viewModelScope.launch {
					registerUser(register)
				}
			}
		}
	}

	fun resetState() {
		_registerResult.update {
			State.Empty()
		}
	}

	private suspend fun registerUser(register: RegisterRequest) {
		repository.register(register,
			onSuccess = {
				_registerResult.update {
					State.Success()
				}
			}, { error ->
			_registerResult.update {
				State.Error(error)
			}
		}).onStart {
			_registerResult.update { State.Loading() }
		}.onEmpty {
			_registerResult.update { State.Empty() }
		}.collect()
	}
}