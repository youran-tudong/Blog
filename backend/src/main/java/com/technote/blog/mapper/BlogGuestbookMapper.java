package com.technote.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.technote.blog.entity.BlogGuestbook;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言 Mapper。
 */
@Mapper
public interface BlogGuestbookMapper extends BaseMapper<BlogGuestbook> {
}
