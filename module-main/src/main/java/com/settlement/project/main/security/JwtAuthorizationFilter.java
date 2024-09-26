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