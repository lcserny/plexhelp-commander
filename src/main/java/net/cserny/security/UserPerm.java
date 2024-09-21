package net.cserny.security;

public enum UserPerm {

    READ("READ"),
    WRITE("WRITE");

    public static final String KEY = "perms";

    private final String type;

    UserPerm(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static UserPerm fromString(String perm) {
        for (UserPerm userPerm : UserPerm.values()) {
            if (userPerm.toString().equals(perm)) {
                return userPerm;
            }
        }
        return null;
    }
}
