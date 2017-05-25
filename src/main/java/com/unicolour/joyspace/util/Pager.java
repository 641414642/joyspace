package com.unicolour.joyspace.util;

import java.util.ArrayList;
import java.util.List;

public class Pager {
    private int currentPageNo;
    private int totalPageCount;

    private List<PageItem> pageItems = new ArrayList<>();

    public List<PageItem> getPageItems() {
        return pageItems;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public Pager(int totalPageCount, int visiblePageCount, int currentPageIndex) {
        this.currentPageNo = currentPageIndex + 1;
        this.totalPageCount = totalPageCount;

        if (visiblePageCount == 0 || totalPageCount <= 0) {
            return;
        }

        int n = visiblePageCount / 2;
        int firstVisiblePage = currentPageIndex - n;
        int lastVisiblePage = currentPageIndex + n;

        if (firstVisiblePage < 0) {
            firstVisiblePage = 0;
            lastVisiblePage = firstVisiblePage + visiblePageCount - 1;
        }

        if (lastVisiblePage >= totalPageCount) {
            lastVisiblePage = totalPageCount - 1;
            firstVisiblePage = lastVisiblePage - visiblePageCount + 1;
            if (firstVisiblePage < 0) {
                firstVisiblePage = 0;
            }
        }

        for (int i = firstVisiblePage; i <= lastVisiblePage; i++) {
            String pageNo = String.valueOf(i+1);
            pageItems.add(new PageItem(pageNo, i == currentPageIndex));
        }
    }
}

class PageItem {
    private String text;
    private boolean selected;

    PageItem(String text, boolean currentPage) {
        this.text = text;
        this.selected = currentPage;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getStyleClass() {
        return selected ? "active" : "";
    }
}
