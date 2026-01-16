package net.cserny.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UtilityProvider {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new BlackbirdModule())
            // this has poor performance
            // .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final ObjectWriter writer = MAPPER.writer();

    // TODO could be improved maybe
    public static String toLoggableString(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            return writer.writeValueAsString(obj);
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
