package com.technote.blog.service;

import com.technote.blog.model.req.LinkSaveReq;
import com.technote.blog.model.resp.LinkResp;

import java.util.List;

/**
 * 友情链接服务。
 */
public interface LinkService {

    /**
     * 查询后台友链列表。
     *
     * @return 友链列表
     */
    List<LinkResp> listAdminLinks();

    /**
     * 新增友链。
     *
     * @param req 友链保存请求
     * @return 友链信息
     */
    LinkResp createLink(LinkSaveReq req);

    /**
     * 修改友链。
     *
     * @param id  友链ID
     * @param req 友链保存请求
     * @return 友链信息
     */
    LinkResp updateLink(Long id, LinkSaveReq req);

    /**
     * 删除友链。
     *
     * @param id 友链ID
     */
    void deleteLink(Long id);

    /**
     * 查询前台可见友链。
     *
     * @return 可见友链列表
     */
    List<LinkResp> listVisibleLinks();
}
