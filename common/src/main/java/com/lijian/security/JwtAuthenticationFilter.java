package com.lijian.security;

import com.lijian.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author lijian
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 从请求中获取JWT
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 从JWT中获取用户名
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 如果用户名不为空且当前没有认证信息
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载用户信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 验证JWT
                    if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 设置认证信息到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug("用户 {} 认证成功", username);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}