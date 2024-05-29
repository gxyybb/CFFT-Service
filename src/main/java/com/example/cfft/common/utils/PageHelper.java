package com.example.cfft.common.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class PageHelper<T> {
    private int pageSize;
    private int pageNum;
    private int totalItems;
    private int totalPages;
    private List<T> list;

    public PageHelper(List<T> list, int pageSize, int pageNum) {
        this.list = list;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.totalItems = list.size();
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }
    public PageHelper(List<T> list,Integer pageNum) {
        this.list = list;
        this.pageSize = 10;
        this.pageNum = pageNum;
        this.totalItems = list.size();
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getPageData() {
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        return list.subList(fromIndex, toIndex);
    }



}
