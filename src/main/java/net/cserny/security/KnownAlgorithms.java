package net.cserny.security;

public class KnownAlgorithms {

    public static final String RSA_FAMILY = "RSA";

    public enum RSAAlgorithms {
        RSA256,
        RSA384,
        RSA512;
    }

    public enum HMACAlgorithms {
        HMAC256,
        HMAC384,
        HMAC512;
    }
}
