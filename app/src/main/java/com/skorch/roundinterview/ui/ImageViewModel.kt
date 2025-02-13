package com.skorch.roundinterview.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skorch.imageloader.ImageLoader
import com.skorch.roundinterview.domain.ImageData
import com.skorch.roundinterview.domain.usecase.GetImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val useCase: GetImagesUseCase) : ViewModel() {

    private val _stateFlow = MutableStateFlow<UiState>(UiState.Loading)
    val stateFlow: StateFlow<UiState> = _stateFlow.asStateFlow()

    init {
        getImagesData()
    }

    private fun getImagesData() {
        viewModelScope.launch {
            _stateFlow.emit(UiState.Loading)
            try {
                useCase.execute()
                    .collect { data -> _stateFlow.emit(UiState.Success(data)) }
            } catch (e: Exception) {
                _stateFlow.emit(UiState.Error(e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    fun handleUserEvent(event: UserEvent) {
        when (event) {
            UserEvent.OnResetClick -> onResetClick()
        }
    }

    private fun onResetClick() {
        ImageLoader.clearCache()
        getImagesData()
    }
}

sealed class UiState {
    data object Loading : UiState()
    data class Success(val images: List<ImageData>) : UiState()
    data class Error(val error: String) : UiState()
}

sealed class UserEvent {
    data object OnResetClick : UserEvent()
}