package com.amora.storyapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepositoryImpl
import com.amora.storyapp.data.remote.model.LoginRequest
import com.amora.storyapp.data.remote.model.LoginResponse
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
class LoginViewModel @Inject constructor(
	private val repository: MainRepositoryImpl
) : ViewModel() {

	private val _loginResult = MutableStateFlow<State<LoginResponse?>>(State.Empty())
	val loginResult = _loginResult.asStateFlow()

	fun login(login: LoginRequest) {
		when {
			login.email.isBlank() -> {
				_loginResult.update {
					State.Error("Email tidak boleh kosong")
				}
			}

			login.password.isBlank() -> {
				_loginResult.update {
					State.Error("Password tidak boleh kosong")
				}
			}

			login.password.length < 8 -> {
				_loginResult.update {
					State.Error("Password harus lebih dari 8 karakter")
				}
			}

			else -> {
				viewModelScope.launch {
					getUser(login)
				}
			}
		}
	}

	fun resetState() {
		_loginResult.update {
			State.Empty()
		}
	}

	private suspend fun getUser(login: LoginRequest) {
		repository.login(login,
			onSuccess = {
				_loginResult.update {
					State.Success()
				}
			}, onError = { error ->
				_loginResult.update {
					State.Error(error)
				}
			}).onStart {
			_loginResult.update { State.Loading() }
		}.onEmpty {
			_loginResult.update { State.Empty() }
		}.collect()
	}

}