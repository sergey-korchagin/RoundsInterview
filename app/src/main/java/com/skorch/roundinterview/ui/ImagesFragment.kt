package com.skorch.roundinterview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skorch.roundinterview.R
import com.skorch.roundinterview.databinding.FragmentImagesBinding
import com.skorch.roundinterview.domain.ImageData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImagesFragment : Fragment(R.layout.fragment_images) {

    private val viewModel: ImageViewModel by viewModels()
    private lateinit var binding: FragmentImagesBinding

    private lateinit var adapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): android.view.View {
        binding = FragmentImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ImageAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stateFlow.collect { state ->
                when (state) {
                    is UiState.Loading -> loading()
                    is UiState.Success -> onImagesReceived(state.images)
                    is UiState.Error -> onError(state.error)
                }
            }
        }

        binding.reset.setOnClickListener {
            viewModel.handleUserEvent(UserEvent.OnResetClick)
        }
    }

    private fun onImagesReceived(imagesData: List<ImageData>) {
        binding.progressBar.isVisible = false
        // adapter not created here for not recreate adapter when reset pressed
        adapter.submitList(imagesData)
        binding.errorView.isVisible = false
    }

    private fun onError(error: String) {
        binding.progressBar.isVisible = false
        binding.errorView.isVisible = true
        binding.errorView.text = error
    }

    private fun loading() {
        binding.progressBar.isVisible = true
        binding.errorView.isVisible = false
    }

    companion object {
        fun newInstance(): ImagesFragment {
            return ImagesFragment()
        }
    }
}