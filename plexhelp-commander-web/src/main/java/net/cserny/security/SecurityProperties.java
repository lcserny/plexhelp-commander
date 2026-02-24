package net.cserny.security;

import lombok.Getter;
import lombok.Setter;
import net.cserny.security.KnownAlgorithms.AsymmetricAlgorithms;
import net.cserny.security.KnownAlgorithms.SymmetricAlgorithms;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "token.security")
@Getter
@Setter
public class SecurityProperties {

    private PemType type;
    private String issuer;
    private PublicKeyProperties publicKey;
    private SecretProperties secret;

    public enum PemType {
        KEYS,
        SECRET;
    }

    @Getter
    @Setter
    public static class PublicKeyProperties {

        private String path;
        private AsymmetricAlgorithms algo;
    }

    @Getter
    @Setter
    public static class SecretProperties {

        private String hash;
        private SymmetricAlgorithms algo;
    }
}
