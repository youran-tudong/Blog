package com.technote.log.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.technote.common.model.PageResp;
import com.technote.log.entity.OperationLogRecord;
import com.technote.log.mapper.OperationLogRecordMapper;
import com.technote.log.model.req.OperationLogPageQueryReq;
import com.technote.log.model.resp.OperationLogResp;
import com.technote.log.service.OperationLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class OperationLogQueryServiceImpl implements OperationLogQueryService {

    private final OperationLogRecordMapper operationLogRecordMapper;

    @Override
    public PageResp<OperationLogResp> pageLogs(OperationLogPageQueryReq req) {
        IPage<OperationLogRecord> page = operationLogRecordMapper.selectPage(
                new Page<>(req.getPageNo(), req.getPageSize()),
                new LambdaQueryWrapper<OperationLogRecord>()
                        .like(StrUtil.isNotBlank(req.getModule()), OperationLogRecord::getModule, req.getModule())
                        .eq(req.getSuccessFlag() != null, OperationLogRecord::getSuccessFlag, req.getSuccessFlag())
                        .orderByDesc(OperationLogRecord::getCreateTime));
        List<OperationLogResp> records = page.getRecords().stream().map(this::toOperationLogResp).toList();
        return PageResp.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private OperationLogResp toOperationLogResp(OperationLogRecord record) {
        OperationLogResp resp = new OperationLogResp();
        resp.setId(record.getId());
        resp.setUserId(record.getUserId());
        resp.setModule(record.getModule());
        resp.setOperation(record.getOperation());
        resp.setRequestMethod(record.getRequestMethod());
        resp.setRequestUri(record.getRequestUri());
        resp.setRequestParams(record.getRequestParams());
        resp.setIp(record.getIp());
        resp.setUserAgent(record.getUserAgent());
        resp.setSuccessFlag(record.getSuccessFlag());
        resp.setErrorMessage(record.getErrorMessage());
        resp.setCreateTime(record.getCreateTime());
        return resp;
    }
}
