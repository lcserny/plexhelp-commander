package net.cserny.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.cserny.support.UtilityProvider.toLoggableString;

@Slf4j
@Aspect
@Component
public class RestControllersLoggingAspect {

    private static final int maxItemsToShow = 100;

    @Around("within(@net.cserny.support.CommanderController *)")
    public Object logControllerMethodInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering: {} with arguments = {}", methodName, captureRequestArgs(joinPoint.getArgs()));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            log.info("Exiting: {} after {}ms with result = {}", methodName, executionTime, captureResponseObject(result));
            return result;
        } catch (Throwable e) {
            log.error("Error occurred executing: {}", methodName);
            throw e;
        }
    }

    private String captureRequestArgs(Object[] args) {
        return toLoggableString(args);
    }

    private String captureResponseObject(Object response) {
        if (response instanceof ResponseEntity<?> e) {
            return captureObjectData(e.getBody());
        }
        return captureObjectData(response);
    }

    private String captureObjectData(Object object) {
        return switch (object) {
            case Collection<?> collection -> captureCollectionData(collection);
            case Object o -> {
                Field field = ReflectionUtils.findField(o.getClass(), "content");
                if (field == null) {
                    yield o.toString();
                }
                ReflectionUtils.makeAccessible(field);
                Collection<?> content = (Collection<?>) ReflectionUtils.getField(field, o);
                yield captureCollectionData(content);
            }
            case null -> "";
        };
    }

    private String captureCollectionData(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return "";
        }

        int size = collection.size();
        String items = "<too many to show>";
        if (size <= maxItemsToShow) {
            items = "\n" + collection.stream().map(Object::toString).collect(Collectors.joining("\n"));
        }

        return String.format("List of %d items containing: %s", size, items);
    }
}
