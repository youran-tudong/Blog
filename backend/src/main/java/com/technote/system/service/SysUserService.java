package com.technote.system.service;

import com.technote.system.model.req.LoginReq;
import com.technote.system.model.req.PasswordUpdateReq;
import com.technote.system.model.req.ProfileUpdateReq;
import com.technote.system.model.resp.LoginResp;
import com.technote.system.model.resp.UserInfoResp;

/**
 * 管理员用户服务。
 */
public interface SysUserService {

    /**
     * 管理员登录。
     *
     * @param req 登录请求
     * @return 登录 token 与用户信息
     */
    LoginResp login(LoginReq req);

    /**
     * 获取当前登录管理员信息。
     *
     * @return 当前登录管理员信息
     */
    UserInfoResp getCurrentUser();

    /**
     * 更新当前登录管理员个人资料。
     *
     * @param req 个人资料更新请求
     * @return 更新后的管理员信息
     */
    UserInfoResp updateCurrentUserProfile(ProfileUpdateReq req);

    /**
     * 修改当前登录管理员密码。修改成功后退出当前会话。
     *
     * @param req 密码修改请求
     */
    void updateCurrentUserPassword(PasswordUpdateReq req);

    /**
     * 退出当前登录会话。
     */
    void logout();
}
