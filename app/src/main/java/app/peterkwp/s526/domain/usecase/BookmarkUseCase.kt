package app.peterkwp.s526.domain.usecase

import app.peterkwp.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    companion object {
        private const val TAG = "SearchBookUseCase"
    }
}