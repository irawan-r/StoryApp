package com.amora.storyapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB: ViewBinding, VM: ViewModel?>: Fragment() {

	protected var binding: VB? = null
	abstract val inflateBinding:(LayoutInflater, ViewGroup?, Boolean) -> VB
	protected abstract val viewModel: VM?

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val binding = inflateBinding(inflater, container, false)
		this.binding = binding
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		initObserver()
	}

	protected abstract fun initView()

	protected abstract fun initObserver()

	override fun onDestroyView() {
		super.onDestroyView()
		this.binding = null
	}
}