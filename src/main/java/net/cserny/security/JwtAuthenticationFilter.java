package net.cserny.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        DecodedJWT jwt = null;
        String username = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                jwt = jwtUtil.verify(token);
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("JWT token error: '{}' {}", token, e.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!jwtUtil.isTokenExpired(jwt)) {
                Set<GrantedAuthority> userRoles = convertToGrantedAuthorities(jwtUtil.extractRoles(jwt));
                Set<GrantedAuthority> userPerms = convertToGrantedAuthorities(jwtUtil.extractPermissions(jwt));

                PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                    username, null, Stream.concat(userRoles.stream(), userPerms.stream()).collect(Collectors.toSet()));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private Set<GrantedAuthority> convertToGrantedAuthorities(Set<? extends Object> set) {
        return set.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
