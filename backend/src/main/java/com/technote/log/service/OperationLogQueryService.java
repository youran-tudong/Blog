package com.technote.log.service;

import com.technote.common.model.PageResp;
import com.technote.log.model.req.OperationLogPageQueryReq;
import com.technote.log.model.resp.OperationLogResp;

/**
 * 操作日志查询服务。
 */
public interface OperationLogQueryService {

    /**
     * 分页查询操作日志。
     *
     * @param req 查询条件
     * @return 操作日志分页结果
     */
    PageResp<OperationLogResp> pageLogs(OperationLogPageQueryReq req);
}
