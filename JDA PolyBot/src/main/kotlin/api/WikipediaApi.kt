package api

data class WikiepediaObj(
    val query: Query,
)

data class Query(
    val pages: HashMap<Int, Page>,
)

data class Page(
    val title: String,
    val extract: String,
)