package com.example.kutira_kushala.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kutira_kushala.data.DirectoryFilter
import com.example.kutira_kushala.data.model.BusinessProfile
import com.example.kutira_kushala.data.model.ProductCategory
import com.example.kutira_kushala.data.repo.BusinessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BuyerViewModel(
    private val businessRepository: BusinessRepository = BusinessRepository(),
) : ViewModel() {
    private val _filter = MutableStateFlow(DirectoryFilter())
    val directoryFilter: StateFlow<DirectoryFilter> = _filter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Using a trigger flow to allow manual refresh
    private val _refreshTrigger = MutableStateFlow(0)

    val businesses: StateFlow<List<BusinessProfile>> = combine(_filter, _refreshTrigger) { f, _ -> f }
        .flatMapLatest { businessRepository.observeDirectory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCategory(category: ProductCategory?) {
        _filter.value = _filter.value.copy(category = category)
    }

    fun setOnlyAcceptingOrders(value: Boolean) {
        _filter.value = _filter.value.copy(onlyAcceptingOrders = value)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshTrigger.value += 1
            delay(1000) // Visual feedback delay
            _isRefreshing.value = false
        }
    }
}
