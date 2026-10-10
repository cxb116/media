package com.media.admin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import io.jsonwebtoken.ExpiredJwtException;

/**
 * JWT工具类
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:media-admin-secret-key-for-jwt-token-generation-2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        return generateToken(claims);
    }

    public String generateSsoToken(String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("account", account);
        claims.put("sso", true);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 60_000L);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512).compact();
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logTokenTimes("JWT解析成功", claims);
            return claims;
        } catch (ExpiredJwtException e) {
            // 过期异常中仍携带 claims，打印时间便于定位服务器时钟/时区问题
            logTokenTimes("JWT已过期", e.getClaims());
            log.warn("JWT过期解析失败: serverNow={}, zone={}, message={}",
                    ZonedDateTime.now(), ZoneId.systemDefault(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("JWT解析失败: serverNow={}, zone={}, message={}",
                    ZonedDateTime.now(), ZoneId.systemDefault(), e.getMessage());
            throw e;
        }
    }

    private void logTokenTimes(String event, Claims claims) {
        Date now = new Date();
        Date issuedAt = claims == null ? null : claims.getIssuedAt();
        Date expirationAt = claims == null ? null : claims.getExpiration();
        Long remainingSeconds = expirationAt == null ? null : (expirationAt.getTime() - now.getTime()) / 1000;
        log.info("{}: serverNow={}, zone={}, issuedAt={}, expirationAt={}, remainingSeconds={}, account={}, sso={}",
                event, now, ZoneId.systemDefault(), issuedAt, expirationAt, remainingSeconds,
                claims == null ? null : claims.get("account"), claims == null ? null : claims.get("sso"));
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public Long getMediaIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Long mediaId = claims.get("mediaId", Long.class);
        return mediaId != null ? mediaId : claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取账号
     */
    public String getAccountFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("account", String.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
