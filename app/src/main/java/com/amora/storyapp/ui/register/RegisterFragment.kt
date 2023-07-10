package com.amora.storyapp.ui.register

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.RegisterRequest
import com.amora.storyapp.databinding.FragmentRegisterBinding
import com.amora.storyapp.ui.base.BaseFragment
import com.amora.storyapp.utils.Constant.lengthCharMin
import com.amora.storyapp.utils.Constant.passwordDelayMillis
import com.amora.storyapp.utils.State
import com.amora.storyapp.utils.showSnackbarNotice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterViewModel>() {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegisterBinding
		get() = FragmentRegisterBinding::inflate
	override val viewModel: RegisterViewModel by viewModels()
	private val mainCoroutine = CoroutineScope(Dispatchers.Main)
	private var passwordJob: Job? = null

	override fun initView() {
		binding?.apply {

			etPassword.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(
					s: CharSequence?,
					start: Int,
					count: Int,
					after: Int
				) {
					// Not needed for this case
				}

				override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
					// Not needed for this case
				}

				override fun afterTextChanged(s: Editable?) {
					val password = s?.toString()
					when {
						password.isNullOrEmpty() -> {
							passwordJob?.cancel()
							passwordJob = mainCoroutine.launch {
								delay(passwordDelayMillis.toLong())
								etPassword.error = "Please enter a password"
							}
						}

						password.length < lengthCharMin -> {
							passwordJob?.cancel()
							passwordJob = mainCoroutine.launch {
								delay(passwordDelayMillis.toLong())
								etPassword.error = "Password need to more than 8 character"
							}
						}
					}
				}
			})

			btDaftar.setOnClickListener {
				val name = etName.text.toString()
				val email = etMail.text.toString()
				val pass = etPassword.text.toString()
				val loginRequest = RegisterRequest().apply {
					this.name = name
					this.email = email
					this.password = pass
				}
				lifecycleScope.launch {
					viewModel.register(loginRequest)
				}
			}
		}
	}

	override fun initObserver() {
		lifecycleScope.launch {
			viewModel.registerResult.onEach { state ->
				when (state) {
					is State.Loading -> {
						loadingState(true)
					}

					is State.Error -> {
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi.toString())
						}
					}

					is State.Success -> {
						val messageApi = state.data?.message
						val message = state.message
						if (message != null) {
							binding?.root?.showSnackbarNotice(message)
						} else {
							binding?.root?.showSnackbarNotice(messageApi.toString())
						}
						findNavController().popBackStack()
						findNavController().navigate(R.id.LoginFragment)
					}

					else -> {
						loadingState(false)
					}
				}
			}.onCompletion {
				viewModel.resetState()
			}.collect()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		viewModel.resetState()
	}

	private fun loadingState(toggle: Boolean) {
		binding?.apply {
			etMail.isEnabled = !toggle
			etName.isEnabled = !toggle
			progressBar.isVisible = toggle
			etPassword.isEnabled = !toggle
			btDaftar.isEnabled = !toggle
		}
	}
}