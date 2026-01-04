package net.cserny.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UtilityProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String toOneLineString(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "Error converting object: " + e.getMessage();
        }
    }

    public static  <T> T getUncheckedThrowing(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
