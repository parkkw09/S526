package app.peterkwp.s526.domain.usecase

import app.peterkwp.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class NewBookUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    fun getNewBook() {
    }

    companion object {
        private const val TAG = "NewBookUseCase"
    }
}