package com.whenwemeet.backend.global.jwt.util;

import static com.whenwemeet.backend.global.exception.ErrorCode.*;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.redis.RefreshRepository;
import com.whenwemeet.backend.global.redis.RefreshToken;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final RefreshRepository refreshRepository;

    @Value("${spring.jwt.expiration.access-token}")
    private long ACCESS_TOKEN_EXPIRE_TIME;
    
    @Value("${spring.jwt.expiration.refresh-token}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    @Value("${spring.jwt.issuer}")
    private String ISSUER;

    public JwtUtil(@Value("${spring.jwt.secret}") String key, RefreshRepository refreshRepository) {
        // String 키를 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.refreshRepository = refreshRepository;
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Authentication authentication) {
        return createToken(authentication, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * Refresh Token 생성 및 저장
     */
    public void generateRefreshToken(Authentication authentication, HttpServletResponse response) {
        String token = createToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        // refreshToken redis 저장
        RefreshToken refreshToken = new RefreshToken(customOAuth2User.getUserId(), token, REFRESH_TOKEN_EXPIRE_TIME);
        refreshRepository.save(refreshToken);

        // 쿠키 생성
        ResponseCookie cookie = createCookie(token);

        // 쿠키 저장
        response.setHeader("Set-Cookie", cookie.toString());
    }

    /**
     * JWT 토큰 생성 공통 메서드
     */
    private String createToken(Authentication authentication, long expireTime) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime);

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        return Jwts.builder()
                .subject(String.valueOf(customOAuth2User.getUserId())) // 사용자 ID
                .issuedAt(now) // 발급 시간
                .expiration(expireDate) // 만료 시간
                .issuer(ISSUER) // 발급자
                .signWith(secretKey, Jwts.SIG.HS512) // 서명 (HS512 알고리즘)
                .compact();
    }

    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e){
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
            return false;
        } catch (SignatureException e){
            log.error("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 구조입니다: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e){
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
            return false;
        } catch (Exception e){
            log.error("JWT 토큰 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    private ResponseCookie createCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME / 1000)
                .build();
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)              // 서명 검증
                .requireIssuer(ISSUER)              // 발급자 검증 (권장)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public boolean verifyRefreshToken(String token) {
        long userIdInToken = getUserId(token);
        RefreshToken tokenInRedis = refreshRepository.findById(userIdInToken)
                .orElseThrow(() -> new NotFoundException(T001));

        return tokenInRedis.getRefreshToken().equals(token);
    }

}
