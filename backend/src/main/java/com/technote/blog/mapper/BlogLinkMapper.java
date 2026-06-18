package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogLink;
import org.apache.ibatis.annotations.Mapper;

/**
 * 友情链接 Mapper。
 */
@Mapper
public interface BlogLinkMapper extends BaseMapper<BlogLink> {
}
