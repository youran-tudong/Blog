package com.technote.blog.service;

import com.technote.blog.model.req.AuditPageQueryReq;
import com.technote.blog.model.req.LinkApplyAuditReq;
import com.technote.blog.model.req.LinkApplySubmitReq;
import com.technote.blog.model.resp.LinkApplyResp;
import com.technote.blog.model.resp.PublicLinkApplyResp;
import com.technote.common.model.PageResp;

/**
 * 友链申请服务。
 */
public interface LinkApplyService {

    /**
     * 提交友链申请，申请默认进入待审核状态。
     *
     * @param req 友链申请内容
     * @return 友链申请信息
     */
    PublicLinkApplyResp submitLinkApply(LinkApplySubmitReq req);

    /**
     * 分页查询后台友链申请。
     *
     * @param req 分页和状态条件
     * @return 友链申请分页结果
     */
    PageResp<LinkApplyResp> pageAdminLinkApplies(AuditPageQueryReq req);

    /**
     * 审核友链申请。
     * 通过后创建一条默认隐藏的正式友链，等待管理员确认展示配置。
     *
     * @param id  申请ID
     * @param req 审核请求
     */
    void auditLinkApply(Long id, LinkApplyAuditReq req);

    /**
     * 删除友链申请。
     *
     * @param id 申请ID
     */
    void deleteLinkApply(Long id);
}
