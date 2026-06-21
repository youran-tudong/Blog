package com.technote.blog.service;

import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.AuditReq;
import com.technote.blog.model.req.GuestbookSubmitReq;
import com.technote.blog.model.resp.GuestbookResp;
import com.technote.blog.model.resp.PublicGuestbookResp;
import com.technote.common.model.PageResp;

import java.util.List;

/**
 * 留言服务。
 */
public interface GuestbookService {

    /**
     * 查询前台已通过留言。
     *
     * @return 留言列表
     */
    List<PublicGuestbookResp> listApprovedGuestbooks();

    /**
     * 提交留言，默认进入待审核状态。
     *
     * @param req 留言提交请求
     * @return 留言信息
     */
    PublicGuestbookResp submitGuestbook(GuestbookSubmitReq req);

    /**
     * 分页查询后台留言审核列表。
     *
     * @param req 查询条件
     * @return 留言分页结果
     */
    PageResp<GuestbookResp> pageAdminGuestbooks(AuditPageQueryReq req);

    /**
     * 审核留言。
     *
     * @param id  留言ID
     * @param req 审核请求
     */
    void auditGuestbook(Long id, AuditReq req);

    /**
     * 删除留言。
     *
     * @param id 留言ID
     */
    void deleteGuestbook(Long id);
}
