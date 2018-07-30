package com.unicolour.joyspace.dto

class Select2QueryResult(
        var results: List<ResultItem> = emptyList(),
        var pagination: ResultPagination = ResultPagination()
)

class ResultItem(
        var id: Int = 0,
        var text: String = ""
)

class ResultPagination(
        var more: Boolean = false
)