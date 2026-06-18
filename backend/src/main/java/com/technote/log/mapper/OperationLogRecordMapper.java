package com.technote.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.log.entity.OperationLogRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface OperationLogRecordMapper extends BaseMapper<OperationLogRecord> {
}
