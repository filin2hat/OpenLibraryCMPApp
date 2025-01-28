package dev.filinhat.openlibapp.book.presentation.bookList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.filinhat.openlibapp.book.domain.Book
import dev.filinhat.openlibapp.book.domain.BookRepository
import dev.filinhat.openlibapp.core.domain.onError
import dev.filinhat.openlibapp.core.domain.onSuccess
import dev.filinhat.openlibapp.core.presentation.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана списка книг.
 */
class BookListViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private var cachedBooks: List<Book> = emptyList()
    private var searchJob: Job? = null

    private val _state = MutableStateFlow(BookListState())
    val state =
        _state
            .onStart {
                if (cachedBooks.isEmpty()) {
                    observeSearchQuery()
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                _state.value,
            )

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnBookClick -> {
                // todo
            }

            is BookListAction.OnSearchQueryChange -> {
                _state.update { bookListState ->
                    bookListState.copy(searchQuery = action.query)
                }
            }

            is BookListAction.OnTabSelected -> {
                _state.update { bookListState ->
                    bookListState.copy(selectedTabIndex = action.index)
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(500)
            .onEach { query ->
                when {
                    query.isBlank() -> {
                        _state.update {
                            it.copy(
                                errorMessage = null,
                                searchResults = cachedBooks,
                            )
                        }
                    }

                    query.length >= 2 -> {
                        searchJob?.cancel()
                        searchJob = searchBooks(query)
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun searchBooks(query: String) =
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            bookRepository
                .searchBooks(query)
                .onSuccess { searchResults ->
                    cachedBooks = searchResults
                    _state.update {
                        it.copy(
                            searchResults = searchResults,
                            errorMessage = null,
                            isLoading = false,
                        )
                    }
                }.onError { error ->
                    _state.update {
                        it.copy(
                            searchResults = emptyList(),
                            isLoading = false,
                            errorMessage = error.toUiText(),
                        )
                    }
                }
        }
}
