package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.model.AuthenticityGrade
import com.example.model.EvidenceType
import com.example.model.Sunnah
import com.example.model.SunnahCategory
import com.example.model.SunnahTime
import com.example.repository.SunnahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val searchQuery: String = "",
    val category: SunnahCategory = SunnahCategory.ALL,
    val timeCategory: SunnahTime = SunnahTime.ALL,
    val evidenceType: EvidenceType = EvidenceType.ALL,
    val authenticity: AuthenticityGrade = AuthenticityGrade.ALL
) {
    val isFiltered: Boolean
        get() = searchQuery.isNotBlank() ||
                category != SunnahCategory.ALL ||
                timeCategory != SunnahTime.ALL ||
                evidenceType != EvidenceType.ALL ||
                authenticity != AuthenticityGrade.ALL
}

class SunnahViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = SunnahRepository(database)

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _dailySunnah = MutableStateFlow<Sunnah?>(null)
    val dailySunnah: StateFlow<Sunnah?> = _dailySunnah.asStateFlow()

    val allSunanFlow = repository.getAllSunan()
    val favoritesFlow = repository.getFavorites()
    val appliedTodayFlow = repository.getAppliedToday()

    // Combined filtered sunan list
    val filteredSunan: StateFlow<List<Sunnah>> = combine(
        allSunanFlow,
        _filterState
    ) { sunan, filters ->
        sunan.filter { item ->
            // Search text
            val matchesQuery = filters.searchQuery.isBlank() ||
                    item.title.contains(filters.searchQuery, ignoreCase = true) ||
                    item.practicalIdea.contains(filters.searchQuery, ignoreCase = true) ||
                    item.evidenceText.contains(filters.searchQuery, ignoreCase = true) ||
                    item.narrator.contains(filters.searchQuery, ignoreCase = true) ||
                    item.source.contains(filters.searchQuery, ignoreCase = true)

            // Category
            val matchesCategory = filters.category == SunnahCategory.ALL || item.category == filters.category

            // Time
            val matchesTime = filters.timeCategory == SunnahTime.ALL ||
                    item.timeCategory == filters.timeCategory ||
                    item.timeCategory == SunnahTime.GENERAL

            // Evidence Type
            val matchesEvidenceType = filters.evidenceType == EvidenceType.ALL || item.evidenceType == filters.evidenceType

            // Authenticity
            val matchesAuth = filters.authenticity == AuthenticityGrade.ALL || item.authenticity == filters.authenticity

            matchesQuery && matchesCategory && matchesTime && matchesEvidenceType && matchesAuth
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            _dailySunnah.value = repository.getDailySunnah()
        }
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: SunnahCategory) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun selectTimeCategory(time: SunnahTime) {
        _filterState.value = _filterState.value.copy(timeCategory = time)
    }

    fun selectEvidenceType(type: EvidenceType) {
        _filterState.value = _filterState.value.copy(evidenceType = type)
    }

    fun selectAuthenticity(grade: AuthenticityGrade) {
        _filterState.value = _filterState.value.copy(authenticity = grade)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun toggleFavorite(sunnah: Sunnah) {
        viewModelScope.launch {
            repository.toggleFavorite(sunnah.id, sunnah.isFavorite)
        }
    }

    fun toggleAppliedToday(sunnah: Sunnah) {
        viewModelScope.launch {
            repository.toggleAppliedToday(sunnah.id, sunnah.isAppliedToday)
            // Refresh daily sunnah state if matched
            if (_dailySunnah.value?.id == sunnah.id) {
                _dailySunnah.value = _dailySunnah.value?.copy(isAppliedToday = !sunnah.isAppliedToday)
            }
        }
    }
}
