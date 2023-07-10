package com.amora.storyapp.ui.login

import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amora.storyapp.MainActivity
import com.amora.storyapp.R
import com.amora.storyapp.data.remote.model.LoginRequest
import com.amora.storyapp.databinding.FragmentLoginBinding
import com.amora.storyapp.ui.base.BaseFragment
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
class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {
	override val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding
		get() = FragmentLoginBinding::inflate
	override val viewModel: LoginViewModel by viewModels()
	private val passwordDelayMillis = 1000
	private var passwordJob: Job? = null

	override fun initView() {
		(requireActivity() as MainActivity).apply {
			supportActionBar()
			supportToolBar(true)
		}
		binding?.apply {
			etPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
				if (hasFocus) {
					// do nothing
				} else {
					val password = etPassword.text?.toString()
					if (!password.isNullOrEmpty() && password.length > 8) {
						passwordJob?.cancel()
						passwordJob = CoroutineScope(Dispatchers.Main).launch {
							etPassword.error = null
							etPassword.setError(null, null)
						}
					}
				}
			}
			etPassword.addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
					// Not needed for this case
				}

				override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
					// Not needed for this case
				}

				override fun afterTextChanged(s: Editable?) {
					val password = s.toString()
					if (etPassword.isFocused && password.isEmpty()) {
						passwordJob?.cancel()
						passwordJob = CoroutineScope(Dispatchers.Main).launch {
							delay(passwordDelayMillis.toLong())
							etPassword.error = "Please enter a password"
						}
					}
				}
			})

			btLogin.setOnClickListener {
				val email = etUsername.text.toString()
				val pass = etPassword.text.toString()
				val loginRequest = LoginRequest().apply {
					this.email = email
					this.password = pass
				}
				lifecycleScope.launch {
					viewModel.login(loginRequest)
				}
			}
			btSignIn.setOnClickListener {
				findNavController().navigate(R.id.RegisterFragment)
			}
		}
	}

	override fun initObserver() {
		lifecycleScope.launch {
			viewModel.loginResult.onEach { state ->
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
						findNavController().popBackStack()
						findNavController().navigate(R.id.DashboardFragment)
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
			progressBar.isVisible = toggle
			etUsername.isEnabled = !toggle
			etPassword.isEnabled = !toggle
			btLogin.isEnabled = !toggle
			btSignIn.isEnabled = !toggle
		}
	}
}