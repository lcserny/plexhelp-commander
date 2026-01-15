package net.cserny.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static net.cserny.support.UtilityProvider.toOneLineString;

@Slf4j
@Aspect
@Component
public class RestControllersLoggingAspect {

//    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void controllerMethods() {
//        // Pointcut for all controller methods
//    }
//
//    @Before("controllerMethods()")
//    public void logBefore(JoinPoint joinPoint) {
//        log.info("Entering method: {} with arguments = {}",
//                joinPoint.getSignature().toShortString(), toOneLineString(joinPoint.getArgs()));
//    }
//
//    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
//    public void logAfterReturning(JoinPoint joinPoint, Object result) {
//        log.info("Exiting method: {} with result = {}",
//                joinPoint.getSignature().toShortString(), toOneLineString(result));
//    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        log.info("==> Request: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            log.info("<== Response: {} executed in {}ms", methodName, executionTime);
            return result;
        } catch (Throwable e) {
            log.error("!!! Exception in {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
