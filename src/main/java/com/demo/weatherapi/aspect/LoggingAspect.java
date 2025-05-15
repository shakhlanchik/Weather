package com.demo.weatherapi.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("WeatherLogger");

    @Pointcut("within(com.demo.weatherapi.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.demo.weatherapi.service..*)")
    public void serviceMethods() {}

    @Before("controllerMethods() || serviceMethods()")
    public void logBefore(org.aspectj.lang.JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.info("➡ Вход в метод: {}.{} с аргументами: {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
    public void logAfter(org.aspectj.lang.JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.info("⬅ Выход из метода: {}.{} с результатом: {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                result);
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint joinPoint, Throwable ex) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.error("Ошибка в методе: {}.{} - {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                ex.getMessage(), ex);
    }

    @Around("controllerMethods() || serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - start;

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.info("Метод: {}.{} выполнен за {} мс",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                timeTaken);

        return result;
    }
}
