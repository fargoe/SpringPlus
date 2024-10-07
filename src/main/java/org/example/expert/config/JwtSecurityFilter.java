package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (isTokenPresent(authorizationHeader)) {
            String jwt = jwtUtil.substringToken(authorizationHeader);
            processToken(jwt, httpRequest, httpResponse);
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    private boolean isTokenPresent(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    private void processToken(String jwt, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        try {
            Claims claims = jwtUtil.extractClaims(jwt);
            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));
            String nickname = claims.get("nickname", String.class);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(userId, email, userRole, nickname, httpRequest);
            }
        } catch (SecurityException | MalformedJwtException e) {
            handleError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            handleError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            handleError(httpResponse, HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.", e);
        } catch (Exception e) {
            handleError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다.", e);
        }
    }

    private void setAuthentication(Long userId, String email, UserRole userRole, String nickname, HttpServletRequest httpRequest) {
        AuthUser authUser = new AuthUser(userId, email, userRole, nickname);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void handleError(HttpServletResponse httpResponse, int status, String message, Exception e) throws IOException {
        log.error(message, e);
        httpResponse.sendError(status, message);
    }
}
