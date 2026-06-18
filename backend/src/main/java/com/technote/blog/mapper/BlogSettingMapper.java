package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置 Mapper。
 */
@Mapper
public interface BlogSettingMapper extends BaseMapper<BlogSetting> {
}
