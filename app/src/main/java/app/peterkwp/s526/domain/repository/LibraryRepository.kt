package app.peterkwp.s526.domain.repository

import app.peterkwp.s526.domain.remote.Api
import javax.inject.Inject

class LibraryRepository @Inject constructor (
    private val api: Api
) {

    suspend fun getNewBook() = api.getNewBooks()
}