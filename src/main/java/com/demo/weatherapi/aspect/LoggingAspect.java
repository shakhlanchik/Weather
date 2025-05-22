package com.demo.weatherapi.aspect;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("WeatherLogger");

    @Pointcut("within(com.demo.weatherapi.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.demo.weatherapi.service..*)")
    public void serviceMethods() {}

    @Pointcut("execution(* com.demo.weatherapi.exception.GlobalExceptionHandler.*(..))")
    public void exceptionHandlerMethods() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void exceptionHandlers() {}

    @AfterThrowing(pointcut = "controllerMethods() || exceptionHandlers()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String errorDetails = getErrorDetails(ex);

        logger.error("Ошибка в {}.{} - {}\nДетали: {}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                ex.getClass().getSimpleName(),
                errorDetails,
                ex);
    }

    private String getErrorDetails(Throwable ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            return ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().stream()
                    .map(error -> {
                        if (error instanceof FieldError) {
                            FieldError fe = (FieldError) error;
                            return fe.getField() + ": " + fe.getDefaultMessage();
                        }
                        return error.getObjectName() + ": " + error.getDefaultMessage();
                    })
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException msrpe
                    = (MissingServletRequestParameterException) ex;
            return "Отсутствует параметр: " + msrpe.getParameterName()
                    + " (" + msrpe.getParameterType() + ")";
        }
        return ex.getMessage();
    }

    @Before("controllerMethods() || serviceMethods()")
    public void logBefore(org.aspectj.lang.JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        Object[] args = (joinPoint != null) ? joinPoint.getArgs() : null;
        String argsStr = (args != null) ? Arrays.toString(args) : "[]";

        logger.info("➡ Вход в метод: {}.{} с аргументами: {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                argsStr);
    }

    @AfterReturning(pointcut = "controllerMethods() || serviceMethods()", returning = "result")
    public void logAfter(org.aspectj.lang.JoinPoint joinPoint, Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (response.getStatusCode().is4xxClientError()) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                logger.warn("Клиентская ошибка в {}.{}: статус {} - тело: {}",
                        signature.getDeclaringType().getSimpleName(),
                        signature.getName(),
                        response.getStatusCodeValue(),
                        response.getBody());
                return;
            }
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.info("⬅ Выход из метода: {}.{} с результатом: {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                result);
    }

    private void log4xxResponse(org.aspectj.lang.JoinPoint joinPoint, ResponseEntity<?> response) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        logger.warn("Клиентская ошибка в {}.{}: статус {} - тело: {}",
                methodSignature.getDeclaringType().getSimpleName(),
                methodSignature.getName(),
                response.getStatusCodeValue(),
                response.getBody());
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