package app.peter.s526.data.source.remote

// https://developer.nytimes.com/docs/books-product/1/overview

class RemoteConst {
    companion object {
        private const val BOOKS = "svc/books"
        private const val VERSION = "v3"
        private const val LIST = "lists"

        const val URL = "https://api.nytimes.com/"
        const val COMMAND = "${BOOKS}/${VERSION}"
        const val KEY = "api-key"
        const val ISBN = "isbn"
        const val AGE = "age-group"

        const val COMMAND_BEST_SELLERS_FULL_OVERVIEW = "/${LIST}/full-overview.json"
        const val COMMAND_BEST_SELLERS_OVERVIEW = "/${LIST}/overview.json"
        const val COMMAND_BEST_SELLERS_FROM_TARGET = "/${LIST}/best-sellers/history.json"
        const val COMMAND_REVIEW = "/reviews.json"
    }
}