package net.cserny;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static String toOneLineString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", " | ");
    }
}
