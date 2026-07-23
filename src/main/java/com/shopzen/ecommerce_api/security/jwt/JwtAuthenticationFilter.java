// src/main/java/com/shopzen/ecommerce_api/security/jwt/JwtAuthenticationFilter.java
package com.shopzen.ecommerce_api.security.jwt;

import com.shopzen.ecommerce_api.service.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private static final Set<String> PUBLIC_ENDPOINTS = new HashSet<>(Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/admin/register",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/verify",              // <-- ADD THIS
        "/api/auth/verify-email",
        "/api/auth/resend-verification",  // <-- ADD THIS
        "/api/auth/make-admin",
        "/api/public/",
        "/api/products",
        "/api/products/featured",
        "/api/products/trending",
        "/api/products/new-arrivals",
        "/api/products/best-sellers",
        "/api/products/search",
        "/api/products/slug/",
        "/api/categories",
        "/api/categories/",
        "/api/brands",
        "/api/brands/",
        "/api/banners",
        "/api/banners/active",
        "/api/search/",
        "/health",
        "/actuator/health",
        "/actuator/info",
        "/api/payments/webhook/",
        "/api/payments/ping",
        "/api/payments/test",
        "/error",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/swagger-ui.html"
    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if (isPublicEndpoint(requestURI)) {
            log.debug("Skipping authentication for public endpoint: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);
            log.debug("JWT token present: {}", StringUtils.hasText(jwt));

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getEmailFromToken(jwt);
                String userId = tokenProvider.getUserIdFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ Authentication successful for user: {} (ID: {})", email, userId);
            } else {
                log.warn("❌ No valid JWT token found for protected endpoint: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication required\", \"message\": \"Please provide a valid JWT token\"}");
                return;
            }
        } catch (Exception e) {
            log.error("❌ Could not set user authentication in security context", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid authentication token\", \"message\": \"" + e.getMessage() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        
        if (requestURI.matches("/api/auth/verify.*")) {
            return true;
        }
        
        if (requestURI.matches("/api/products(/[a-zA-Z0-9-]+)?")) {
            return true;
        }
        if (requestURI.matches("/api/categories(/[a-zA-Z0-9-]+)?")) {
            return true;
        }
        if (requestURI.matches("/api/brands(/[a-zA-Z0-9-]+)?")) {
            return true;
        }
        if (requestURI.matches("/api/banners(/[a-zA-Z0-9-]+)?")) {
            return true;
        }
        
        return false;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Skipping filter for OPTIONS request: {}", requestURI);
            return true;
        }
        return isPublicEndpoint(requestURI);
    }
}