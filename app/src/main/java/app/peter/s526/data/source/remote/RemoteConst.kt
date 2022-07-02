package app.peter.s526.data.source.remote

class RemoteConst {
    companion object {
        private const val BOOKS = "svc/books"
        private const val VERSION = "v3"
        private const val LIST = "lists"

        const val URL = "https://api.nytimes.com/"
        const val COMMAND = "${BOOKS}/${VERSION}"
        const val KEY = "api-key"

        const val COMMAND_BEST_SELLERS = "/${LIST}/best-sellers/history.json"
    }
}