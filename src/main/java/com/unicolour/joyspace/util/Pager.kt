package com.unicolour.joyspace.util

import java.util.ArrayList

class Pager(totalPageCount: Int, visiblePageCount: Int, currentPageIndex: Int) {
    val currentPageNo: Int

    val pageItems = ArrayList<PageItem>()

    init {
        this.currentPageNo = currentPageIndex + 1

        if (visiblePageCount != 0 && totalPageCount > 0) {
            val n = visiblePageCount / 2
            var firstVisiblePage = currentPageIndex - n
            var lastVisiblePage = currentPageIndex + n

            if (firstVisiblePage < 0) {
                firstVisiblePage = 0
                lastVisiblePage = firstVisiblePage + visiblePageCount - 1
            }

            if (lastVisiblePage >= totalPageCount) {
                lastVisiblePage = totalPageCount - 1
                firstVisiblePage = lastVisiblePage - visiblePageCount + 1
                if (firstVisiblePage < 0) {
                    firstVisiblePage = 0
                }
            }

            for (i in firstVisiblePage..lastVisiblePage) {
                val pageNo = (i + 1).toString()
                pageItems.add(PageItem(pageNo, i == currentPageIndex))
            }
        }
    }
}

class PageItem(val text: String, val isSelected: Boolean) {
    val styleClass: String
        get() = if (isSelected) "active" else ""
}
