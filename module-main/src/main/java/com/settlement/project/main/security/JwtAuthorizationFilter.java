package com.settlement.project.main.security;

import com.settlement.project.common.jwt.JwtUtil;
import com.settlement.project.main.user.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
//        // JWT 토큰을 헤더에서 가져옴
//        String tokenValue = jwtUtil.getJwtFromHeader(req);
//        log.info("Received token from header: {}", tokenValue);
//
//
//        if (tokenValue == null) {
//            // 헤더에 토큰이 없으면 쿠키에서 확인
//            Cookie[] cookies = req.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if (JwtUtil.AUTHORIZATION_HEADER.equals(cookie.getName())) {
//                        tokenValue = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//        }
//        if (StringUtils.hasText(tokenValue)) {
//            // 토큰 유효성 검증
//            log.info("Token detected, validating...");
//            if (tokenValue.startsWith("Bearer ")) {
//                tokenValue = tokenValue.substring(7);
//            }
//            if (!jwtUtil.validateToken(tokenValue)) {
//                log.error("Token validation failed");
//                return;
//            }
//            log.info("Token is valid");
//
//            // 토큰에서 사용자 정보 추출
//            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
//            log.info("Claims extracted from token: {}", info);
//
//            try {
//                // 인증 설정
//                log.info("Setting authentication for user: {}", info.getSubject());
//                setAuthentication(info.getSubject());
//                log.info("Authentication set in SecurityContextHolder: {}", SecurityContextHolder.getContext().getAuthentication());
//            } catch (Exception e) {
//                log.error("Failed to set authentication: " + e.getMessage());
//                return;
//            }
//        } else {
//            log.warn("No token found in request");
//        }
//
//        // 필터 체인에 따라 다음 필터로 이동하기 전 SecurityContextHolder 상태 확인
//        log.info("Before filter chain continuation, SecurityContextHolder: {}", SecurityContextHolder.getContext().getAuthentication());
//        filterChain.doFilter(req, res);
//        log.info("After filter chain continuation, SecurityContextHolder: {}", SecurityContextHolder.getContext().getAuthentication());
//    }




    //인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

     //인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        String tokenValue = null;

        // 헤더에서 토큰 추출 시도
        tokenValue = jwtUtil.getJwtFromHeader(req);
        log.info("Received token from header: {}", tokenValue);

        // 헤더에 토큰이 없으면 쿠키에서 추출 시도
        if (tokenValue == null) {
            tokenValue = extractTokenFromCookie(req);
            log.info("Received token from cookie: {}", tokenValue);
        }

        if (StringUtils.hasText(tokenValue)) {
            try {
                if (jwtUtil.validateToken(tokenValue)) {
                    Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
                    log.info("Claims extracted from token: {}", info);
                    setAuthentication(info.getSubject());
                    log.info("Authentication set in SecurityContextHolder: {}", SecurityContextHolder.getContext().getAuthentication());
                } else {
                    log.warn("Invalid token");
                }
            } catch (Exception e) {
                log.error("Failed to process token: ", e);
            }
        } else {
            log.warn("No token found in request");
        }

        filterChain.doFilter(req, res);
        log.info("After filter chain continuation, SecurityContextHolder: {}", SecurityContextHolder.getContext().getAuthentication());
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtUtil.AUTHORIZATION_HEADER.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}