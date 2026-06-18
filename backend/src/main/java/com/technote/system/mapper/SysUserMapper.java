package com.technote.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

