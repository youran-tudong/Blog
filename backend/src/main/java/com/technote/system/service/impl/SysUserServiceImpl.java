package com.technote.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.technote.common.exception.BaseException;
import com.technote.system.entity.SysUser;
import com.technote.system.enums.UserStatusEnum;
import com.technote.system.mapper.SysUserMapper;
import com.technote.system.model.req.LoginReq;
import com.technote.system.model.req.PasswordUpdateReq;
import com.technote.system.model.req.ProfileUpdateReq;
import com.technote.system.model.resp.LoginResp;
import com.technote.system.model.resp.UserInfoResp;
import com.technote.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 管理员用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    /**
     * 登录涉及最后登录时间更新，放在事务内保证状态一致。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(LoginReq req) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername())
                .last("LIMIT 1"));
        if (user == null || !matchesPassword(req.getPassword(), user.getPassword())) {
            throw new BaseException(401, "用户名或密码错误");
        }
        if (!UserStatusEnum.ENABLED.getCode().equals(user.getStatus())) {
            throw new BaseException(403, "账号已被禁用");
        }

        StpUtil.login(user.getId());
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginResp resp = new LoginResp();
        resp.setTokenName(tokenInfo.getTokenName());
        resp.setTokenValue(tokenInfo.getTokenValue());
        resp.setUserInfo(toUserInfo(user));
        return resp;
    }

    @Override
    public UserInfoResp getCurrentUser() {
        return toUserInfo(getCurrentEnabledUser(false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp updateCurrentUserProfile(ProfileUpdateReq req) {
        SysUser user = getCurrentEnabledUser(false);
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .eq(SysUser::getStatus, UserStatusEnum.ENABLED.getCode())
                .set(SysUser::getNickname, req.getNickname().trim())
                .set(SysUser::getAvatarUrl, normalizeOptional(req.getAvatarUrl()))
                .set(SysUser::getEmail, normalizeOptional(req.getEmail()))
                .set(SysUser::getBio, normalizeOptional(req.getBio()))
                .set(SysUser::getUpdateBy, user.getId());
        if (sysUserMapper.update(null, wrapper) == 0) {
            throw new BaseException(409, "账号状态已变化，请重新登录");
        }
        return toUserInfo(getCurrentEnabledUser(false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserPassword(PasswordUpdateReq req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BaseException(400, "两次输入的新密码不一致");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new BaseException(400, "新密码不能与当前密码相同");
        }
        SysUser user = getCurrentEnabledUser(true);
        if (!matchesPassword(req.getOldPassword(), user.getPassword())) {
            throw new BaseException(400, "当前密码不正确");
        }
        String encodedPassword = BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt());
        int updated = sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .eq(SysUser::getStatus, UserStatusEnum.ENABLED.getCode())
                .eq(SysUser::getPassword, user.getPassword())
                .set(SysUser::getPassword, encodedPassword)
                .set(SysUser::getUpdateBy, user.getId()));
        if (updated == 0) {
            throw new BaseException(409, "账号状态已变化，请重新登录");
        }
        logoutAfterCommit();
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private SysUser getCurrentEnabledUser(boolean forUpdate) {
        StpUtil.checkLogin();
        Long userId = currentUserId();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .last(forUpdate ? "LIMIT 1 FOR UPDATE" : "LIMIT 1");
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            StpUtil.logout();
            throw new BaseException(401, "登录用户不存在");
        }
        if (!UserStatusEnum.ENABLED.getCode().equals(user.getStatus())) {
            StpUtil.logout();
            throw new BaseException(403, "账号已被禁用");
        }
        return user;
    }

    private UserInfoResp toUserInfo(SysUser user) {
        UserInfoResp resp = new UserInfoResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setEmail(user.getEmail());
        resp.setBio(user.getBio());
        resp.setRoleCode(user.getRoleCode());
        return resp;
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (RuntimeException e) {
            // 密码格式异常时按登录失败处理，避免泄露底层加密细节。
            return false;
        }
    }

    private String normalizeOptional(String value) {
        return StrUtil.isBlank(value) ? null : value.trim();
    }

    private Long currentUserId() {
        return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
    }

    private void logoutAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            StpUtil.logout();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                StpUtil.logout();
            }
        });
    }
}
