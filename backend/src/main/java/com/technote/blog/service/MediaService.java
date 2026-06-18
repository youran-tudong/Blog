package com.technote.blog.service;

import com.technote.blog.model.req.MediaPageQueryReq;
import com.technote.blog.model.resp.MediaResp;
import com.technote.common.model.PageResp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体文件服务。
 */
public interface MediaService {

    /**
     * 上传文章图片。
     *
     * @param file 上传文件
     * @return 媒体文件信息
     */
    MediaResp uploadImage(MultipartFile file);

    /**
     * 分页查询媒体库。
     *
     * @param req 查询条件
     * @return 媒体分页结果
     */
    PageResp<MediaResp> pageMedia(MediaPageQueryReq req);

    /**
     * 删除媒体文件。已被引用的文件不允许删除。
     *
     * @param id 媒体ID
     */
    void deleteMedia(Long id);

    /**
     * 根据文章内容变化刷新媒体引用次数。
     *
     * @param oldCoverUrl 旧封面地址
     * @param oldContent  旧正文
     * @param newCoverUrl 新封面地址
     * @param newContent  新正文
     */
    void refreshArticleMediaQuotes(String oldCoverUrl, String oldContent, String newCoverUrl, String newContent);
}
