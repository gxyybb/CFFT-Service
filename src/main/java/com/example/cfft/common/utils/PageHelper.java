package com.example.cfft.common.utils;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class PageHelper<T> {
    private int pageSize;
    private int pageNum;
    private int totalItems;
    private int totalPages;
    private List<T> list;

    public PageHelper(List<T> list, int pageSize, int pageNum) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        if (pageNum <= 0) {
            throw new IllegalArgumentException("pageNum must be greater than 0");
        }

        this.list = list;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.totalItems = list.size();
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public PageHelper(List<T> list, Integer pageNum) {
        this(list, 10, pageNum);
    }

    public List<T> getPageData() {
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= totalItems) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        return list.subList(fromIndex, toIndex);
    }
}
