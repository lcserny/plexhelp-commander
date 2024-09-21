package net.cserny.security;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
@Component
public class PemUtils {

    private final SecurityProperties properties;

    @Autowired
    public PemUtils(SecurityProperties properties) {
        this.properties = properties;
    }

    @Getter
    private Key verificationKey;

    @PostConstruct
    public void init() throws IOException {
        initVerificationKey();
    }

    private void initVerificationKey() throws IOException {
        switch (this.properties.getType()) {
            case KEYS -> {
                byte[] bytes = parsePEMFile(new File(this.properties.getPublicKey().getPath()));
                this.verificationKey = getPublicKey(bytes, this.properties.getPublicKey().getAlgo());
            }
            case SECRET -> this.verificationKey = Keys.hmacShaKeyFor(this.properties.getSecret().getHash().getBytes(StandardCharsets.UTF_8));
            default -> throw new IllegalStateException("Unexpected type: " + this.properties.getType());
        }
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

    private PublicKey getPublicKey(byte[] keyBytes, String algorithm) {
        PublicKey publicKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            publicKey = kf.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not reconstruct the public key, the given algorithm could not be found.");
        } catch (InvalidKeySpecException e) {
            log.error("Could not reconstruct the public key");
        }

        return publicKey;
    }
}
