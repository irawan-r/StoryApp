package com.amora.storyapp

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amora.storyapp.data.local.MainRepository
import com.amora.storyapp.data.remote.model.User
import com.bumptech.glide.Glide.init
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val repository: MainRepository
) : ViewModel() {

	private val _navGraphDestination: MutableStateFlow<Int?> = MutableStateFlow(null)
	val navGraphDestination = _navGraphDestination.asStateFlow()

	init {
		getSession()
	}

	fun getSession() {
		viewModelScope.launch {
			repository.getSession().collect { userSession ->
				if (userSession != null) {
					_navGraphDestination.update {
						R.id.DashboardFragment
					}
				} else {
					_navGraphDestination.update {
						R.id.LoginFragment
					}
				}
			}
		}
	}
}