package net.cserny.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

import static net.cserny.security.KnownAlgorithms.RSA_FAMILY;

@Slf4j
@Component
public class JwtUtil {

    @Value("${spring.application.name}")
    private String appName;

    private final String issuer;
    private final JWTVerifier verifier;

    @Autowired
    public JwtUtil(SecurityProperties properties) throws IOException {
        this.issuer = properties.getIssuer();
        this.verifier = JWT.require(this.initAlgorithm(properties)).build();
    }

    private Algorithm initAlgorithm(SecurityProperties properties) throws IOException {
        return switch (properties.getType()) {
            case KEYS -> this.getRSAAlgorithm(properties);
            case SECRET -> this.getHMACAlgorithm(properties);
        };
    }

    private Algorithm getRSAAlgorithm(SecurityProperties properties) throws IOException {
        byte[] bytes = parsePEMFile(new File(properties.getPublicKey().getPath()));
        return switch (properties.getPublicKey().getAlgo()) {
            case RSA256 -> Algorithm.RSA256(getPublicKey(bytes, RSA_FAMILY));
            case RSA384 -> Algorithm.RSA384(getPublicKey(bytes, RSA_FAMILY));
            case RSA512 -> Algorithm.RSA512(getPublicKey(bytes, RSA_FAMILY));
        };
    }

    private Algorithm getHMACAlgorithm(SecurityProperties properties) {
        return switch (properties.getSecret().getAlgo()) {
            case HMAC256 -> Algorithm.HMAC256(properties.getSecret().getHash());
            case HMAC384 -> Algorithm.HMAC384(properties.getSecret().getHash());
            case HMAC512 -> Algorithm.HMAC512(properties.getSecret().getHash());
        };
    }

    private byte[] parsePEMFile(File pemFile) throws IOException {
        if (!pemFile.isFile() || !pemFile.exists()) {
            throw new FileNotFoundException(String.format("The file '%s' doesn't exist.", pemFile.getAbsolutePath()));
        }
        try (PemReader reader = new PemReader(new FileReader(pemFile))) {
            PemObject pemObject = reader.readPemObject();
            return pemObject.getContent();
        }
    }

    private RSAPublicKey getPublicKey(byte[] keyBytes, String algorithm) {
        RSAPublicKey publicKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            publicKey = (RSAPublicKey) kf.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not reconstruct the public key, the given algorithm could not be found.");
        } catch (InvalidKeySpecException e) {
            log.error("Could not reconstruct the public key");
        }

        return publicKey;
    }

    public String extractUsername(DecodedJWT jwt) {
        return jwt.getSubject();
    }

    public Date extractExpiration(DecodedJWT jwt) {
        return jwt.getExpiresAt();
    }

    public List<String> extractAudience(DecodedJWT jwt) {
        return jwt.getAudience();
    }

    public String extractIssuer(DecodedJWT jwt) {
        return jwt.getIssuer();
    }

    public Set<UserRole> extractRoles(DecodedJWT jwt) {
        List<String> roles = jwt.getClaim(UserRole.KEY).asList(String.class);
        return roles.stream()
                .map(UserRole::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<UserPerm> extractPermissions(DecodedJWT jwt) {
        List<String> perms = jwt.getClaim(UserPerm.KEY).asList(String.class);
        return perms.stream()
                .map(UserPerm::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public DecodedJWT verify(String token) {
        return this.verifier.verify(token);
    }

    public Boolean validate(String username, DecodedJWT jwt) {
        String currentIssuer = this.extractIssuer(jwt);
        if (!currentIssuer.equals(this.issuer)) {
            log.warn("User with id '{}' did not provide correct issuer, it provided: '{}'", username, currentIssuer);
            return false;
        }

        List<String> currentAudience = this.extractAudience(jwt);
        if (!currentAudience.contains(this.appName)) {
            log.warn("Token for User with id '{}' did not come from correct application, audience provided: '{}'", username, currentAudience);
            return false;
        }

        return this.extractExpiration(jwt).before(new Date());
    }
}
