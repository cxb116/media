package com.media.admin.service;

import com.media.admin.common.Result;
import com.media.admin.dto.MediaLoginRequest;
import com.media.admin.dto.MediaLoginResponse;
import com.media.admin.dto.MediaRegisterRequest;
import com.media.admin.dto.MediaUpdateRequest;
import com.media.admin.entity.SspMedia;
import com.media.admin.repository.SspMediaRepository;
import com.media.admin.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 媒体用户服务
 */
@Slf4j
@Service
public class SspMediaService {

    @Autowired
    private SspMediaRepository sspMediaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 媒体用户注册
     */
    @Transactional
    public Result<?> register(MediaRegisterRequest request) {
        // 检查账号是否已存在
        if (sspMediaRepository.existsByAccount(request.getAccount())) {
            return Result.error("账号已存在");
        }

        // 创建媒体用户实体
        SspMedia sspMedia = new SspMedia();
        sspMedia.setName(request.getName());
        sspMedia.setAccount(request.getAccount());
        sspMedia.setPassword(request.getPassword());
        sspMedia.setMediaCompanyName(request.getMediaCompanyName());
        sspMedia.setMediaCompanyShort(request.getMediaCompanyShort());
        sspMedia.setMediaCompanyCode(request.getMediaCompanyCode());
        sspMedia.setMediaCompanyLicense(request.getMediaCompanyLicense());
        sspMedia.setMediaCompanyAddress(request.getMediaCompanyAddress());
        sspMedia.setMediaOwnerName(request.getMediaOwnerName());
        sspMedia.setContactName(request.getContactName());
        sspMedia.setContactPhone(request.getContactPhone());
        sspMedia.setContactEmail(request.getContactEmail());
        sspMedia.setEnable(2); // 默认状态：2审核中，需要管理员审核通过后才能登录
        sspMedia.setRemark(request.getRemark());
        sspMedia.setCreateBy(request.getAccount());

        // 保存用户
        SspMedia savedMedia = sspMediaRepository.save(sspMedia);

        Map<String, Object> data = new HashMap<>();
        data.put("id", savedMedia.getId());
        data.put("account", savedMedia.getAccount());
        data.put("enable", savedMedia.getEnable());
        data.put("message", "注册成功，等待审核");

        return Result.success("注册成功，等待审核", data);
    }

    /**
     * 媒体用户登录
     */
    public Result<MediaLoginResponse> login(MediaLoginRequest request) {
        // 查找用户
        Optional<SspMedia> mediaOptional = sspMediaRepository.findByAccount(request.getAccount());
        if (!mediaOptional.isPresent()) {
            return Result.error("账号或密码错误");
        }

        SspMedia sspMedia = mediaOptional.get();

        // 验证密码（明文比较）
        if (!Objects.equals(request.getPassword(), sspMedia.getPassword())) {
            return Result.error("账号或密码错误");
        }

        // 检查账号状态
        if (sspMedia.getEnable() == 0) {
            return Result.error("账号已被禁用");
        }
        if (sspMedia.getEnable() == 2) {
            return Result.error("账号正在审核中");
        }
        if (sspMedia.getEnable() == 3) {
            return Result.error("账号审核未通过");
        }

        // 生成Token
        String token = jwtUtil.generateToken(sspMedia.getId(), sspMedia.getAccount());

        // 构建响应
        MediaLoginResponse response = MediaLoginResponse.builder()
                .id(sspMedia.getId())
                .name(sspMedia.getName())
                .account(sspMedia.getAccount())
                .mediaCompanyName(sspMedia.getMediaCompanyName())
                .enable(sspMedia.getEnable())
                .token(token)
                .build();

        return Result.success("登录成功", response);
    }

    public Result<MediaLoginResponse> ssoLogin(String ssoToken) {
        if (ssoToken == null || ssoToken.trim().isEmpty()) {
            log.warn("SSO票据为空");
            return Result.error(401, "SSO 票据无效或已过期");
        }
        if (!jwtUtil.validateToken(ssoToken)) {
            log.warn("SSO票据校验失败: tokenLength={}, serverNow={}", ssoToken.length(), new java.util.Date());
            return Result.error(401, "SSO 票据无效或已过期");
        }
        try {
            io.jsonwebtoken.Claims claims = jwtUtil.getClaimsFromToken(ssoToken);
            log.info("SSO票据解析完成: account={}, sso={}, issuedAt={}, expirationAt={}, serverNow={}",
                    claims.get("account"), claims.get("sso"), claims.getIssuedAt(), claims.getExpiration(), new java.util.Date());
            if (!Boolean.TRUE.equals(claims.get("sso", Boolean.class))) return Result.error(401, "SSO 票据无效");
            Optional<SspMedia> optional = sspMediaRepository.findByAccount(claims.get("account", String.class));
            if (!optional.isPresent()) return Result.error("账号不存在");
            SspMedia media = optional.get();
            if (media.getEnable() != 1) return Result.error("账号当前不可登录");
            String token = jwtUtil.generateToken(media.getId(), media.getAccount());
            return Result.success("登录成功", MediaLoginResponse.builder().id(media.getId()).name(media.getName()).account(media.getAccount()).mediaCompanyName(media.getMediaCompanyName()).enable(media.getEnable()).token(token).build());
        } catch (Exception e) {
            log.warn("SSO 票据解析失败", e);
            return Result.error(401, "SSO 票据无效");
        }
    }

    /**
     * 根据ID获取媒体用户信息
     */
    public Result<SspMedia> getMediaInfo(Long id) {
        Optional<SspMedia> mediaOptional = sspMediaRepository.findById(id);
        if (!mediaOptional.isPresent()) {
            return Result.error("用户不存在");
        }
        SspMedia sspMedia = mediaOptional.get();
        // 清除密码信息
        sspMedia.setPassword(null);
        return Result.success(sspMedia);
    }

    /**
     * 验证Token
     */
    public Result<Map<String, Object>> validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        Optional<SspMedia> mediaOptional = sspMediaRepository.findById(userId);
        if (!mediaOptional.isPresent()) {
            return Result.error("用户不存在");
        }

        SspMedia sspMedia = mediaOptional.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", sspMedia.getId());
        data.put("account", sspMedia.getAccount());
        data.put("name", sspMedia.getName());
        data.put("enable", sspMedia.getEnable());

        return Result.success(data);
    }

    /**
     * 更新媒体用户信息
     */
    @Transactional
    public Result<SspMedia> updateMediaInfo(MediaUpdateRequest request) {
        Optional<SspMedia> mediaOptional = sspMediaRepository.findById(request.getId());
        if (!mediaOptional.isPresent()) {
            return Result.error("用户不存在");
        }

        SspMedia sspMedia = mediaOptional.get();

        // 更新允许修改的字段
        if (request.getName() != null) {
            sspMedia.setName(request.getName());
        }
        if (request.getMediaCompanyAddress() != null) {
            sspMedia.setMediaCompanyAddress(request.getMediaCompanyAddress());
        }
        if (request.getContactPhone() != null) {
            sspMedia.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            sspMedia.setContactEmail(request.getContactEmail());
        }
        if (request.getRemark() != null) {
            sspMedia.setRemark(request.getRemark());
        }

        // 保存更新
        SspMedia updatedMedia = sspMediaRepository.save(sspMedia);

        // 清除密码信息后返回
        updatedMedia.setPassword(null);
        return Result.success("更新成功", updatedMedia);
    }
}
