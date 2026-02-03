package com.crs.iamservice.config;

import com.crs.iamservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        log.info("🌐 JWT Filter - Processing request: {} {}", request.getMethod(), requestPath);

        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            // 1. Kiểm tra header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("⚠️ No valid Authorization header for: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Trích xuất Token và Email
            jwt = authHeader.substring(7);
            log.info("📝 Token present, length: {}", jwt.length());

            userEmail = jwtService.extractUsername(jwt);
            log.info("👤 Extracted email: {}", userEmail);

            // 3. Nếu có email và chưa được xác thực trong SecurityContext
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.info("📋 Loaded UserDetails for: {}", userEmail);

                // 4. Kiểm tra tính hợp lệ của Token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // DEBUG: Log authorities
                    log.info("🔐 JWT Filter - User: {}, Authorities: {}", userEmail, userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Xác lập quyền hạn vào hệ thống
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("✅ Authentication set for user: {}", userEmail);
                } else {
                    log.error("❌ Token is invalid for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("💥 Exception in JWT Filter for path {}: {}", requestPath, e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
