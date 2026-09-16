package net.cserny.support;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UtilityProvider {

    private static final ObjectWriter writer = JsonMapper.builder()
            .addModule(new BlackbirdModule())
            .disable(SerializationFeature.INDENT_OUTPUT)
            .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()
            .writer();

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

    public static String escaped(String text) {
        return "'" + text.replace("'", "'\"'\"'") + "'";
    }
}
