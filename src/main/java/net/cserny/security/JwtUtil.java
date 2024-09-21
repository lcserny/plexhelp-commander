package net.cserny.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final JwtParser parser;

    @Autowired
    public JwtUtil(PemUtils pemUtils) {
        JwtParserBuilder parser = Jwts.parser();

        switch (pemUtils.getVerificationKey()) {
            case SecretKey secretKey -> parser.verifyWith(secretKey);
            case PublicKey publicKey -> parser.verifyWith(publicKey);
            default -> throw new IllegalStateException("Unexpected type: " + pemUtils.getVerificationKey());
        }

        this.parser = parser.build();

    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Set<String> extractAudience(String token) {
        return extractClaim(token, Claims::getAudience);
    }

    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public Set<UserRole> extractRoles(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get(UserRole.KEY, ArrayList.class));
        return roles.stream()
                .map(UserRole::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<UserPerm> extractPermissions(String token) {
        List<String> perms = extractClaim(token, claims -> claims.get(UserPerm.KEY, ArrayList.class));
        return perms.stream()
                .map(UserPerm::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return this.parser.parseSignedClaims(token).getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
