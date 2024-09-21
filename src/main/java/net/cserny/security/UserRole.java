package net.cserny.security;

public enum UserRole {

    ADMIN("ADMIN"),
    STANDARD("STANDARD"),
    GUEST("GUEST");

    public static final String KEY = "roles";

    private final String type;

    UserRole(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.toString().equals(role)) {
                return userRole;
            }
        }
        return null;
    }
}
