package com.example.core.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.repository.AuthenticationRepository
import com.example.core.repository.GetDataRepository
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val repoAuth: AuthenticationRepository,
    private val repoGetData: GetDataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == MyViewModel::class.java) {
            return MyViewModel(repoAuth = repoAuth, repoGetData = repoGetData) as T
        }
        throw RuntimeException("Unknown vie model class $modelClass")
    }
}