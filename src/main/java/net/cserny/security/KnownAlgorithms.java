package net.cserny.security;

public class KnownAlgorithms {

    public static final String RSA_FAMILY = "RSA";
    public static final String ECDSA_FAMILY = "EC";

    public enum AsymmetricAlgorithms {
        RSA256,
        RSA384,
        RSA512,
        ECDSA256,;
    }

    public enum SymmetricAlgorithms {
        NONE,
        HMAC256,
        HMAC384,
        HMAC512;
    }
}
