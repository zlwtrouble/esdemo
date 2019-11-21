package com.zhao.esdemo.base;

import lombok.Getter;
import lombok.Setter;

public class Pager {


    /**
     * 当前页码
     */
    @Setter
    @Getter
    private Integer pageIndex;

    /**
     * 每页数量
     */
    @Setter
    @Getter
    private Integer pageSize;

    /**
     * 排序
     */
    @Getter
    @Setter
    private Integer orderByClause;

    public Pager() {

    }

    public Pager(Integer orderByClause) {
        this.orderByClause = orderByClause;
    }

    public Pager(Integer pageIndex, Integer pageSize, Integer orderByClause) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.orderByClause = orderByClause;
    }
    public Pager(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}