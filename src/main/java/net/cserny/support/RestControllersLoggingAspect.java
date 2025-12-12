package net.cserny.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static net.cserny.support.UtilityProvider.toOneLineString;

@Slf4j
@Aspect
@Component
public class RestControllersLoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
        // Pointcut for all controller methods
    }

    @Before("controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering method: {} with arguments = {}",
                joinPoint.getSignature().toShortString(), toOneLineString(Arrays.toString(joinPoint.getArgs())));
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: {} with result = {}",
                joinPoint.getSignature().toShortString(), toOneLineString(result));
    }
}
