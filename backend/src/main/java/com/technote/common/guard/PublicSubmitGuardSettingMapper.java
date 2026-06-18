package com.technote.common.guard;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公开提交风控设置 Mapper。
 */
@Mapper
public interface PublicSubmitGuardSettingMapper extends BaseMapper<PublicSubmitGuardSetting> {
}
