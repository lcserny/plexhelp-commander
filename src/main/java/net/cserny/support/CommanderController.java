package net.cserny.support;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public @interface CommanderController {

    String BASE_PATH = "/api/v1";

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};
}
