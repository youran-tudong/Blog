package com.technote.common.model;

import lombok.Data;

import java.util.List;

/**
 * 通用分页返回结果。
 *
 * @param <T> 记录类型
 */
@Data
public class PageResp<T> {

    /**
     * 当前页记录。
     */
    private List<T> records;

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 当前页码。
     */
    private Long current;

    /**
     * 每页数量。
     */
    private Long size;

    public static <T> PageResp<T> of(List<T> records, Long total, Long current, Long size) {
        PageResp<T> resp = new PageResp<>();
        resp.setRecords(records);
        resp.setTotal(total);
        resp.setCurrent(current);
        resp.setSize(size);
        return resp;
    }
}

