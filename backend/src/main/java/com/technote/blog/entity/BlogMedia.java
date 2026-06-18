package com.technote.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 媒体文件实体，对应 blog_media 表。
 */
@Data
@TableName("blog_media")
public class BlogMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * 存储文件名。
     */
    private String fileName;

    /**
     * 文件访问路径。
     */
    private String filePath;

    /**
     * 文件大小，单位字节。
     */
    private Long fileSize;

    /**
     * MIME 类型。
     */
    private String mimeType;

    /**
     * 文件扩展名。
     */
    private String fileExt;

    /**
     * 被内容引用次数。
     */
    private Integer quoteCount;

    /**
     * 上传人。
     */
    private Long uploadBy;

    private LocalDateTime createTime;

    /**
     * 逻辑删除：0未删除 1已删除。
     */
    @TableLogic
    private Integer deleted;
}
