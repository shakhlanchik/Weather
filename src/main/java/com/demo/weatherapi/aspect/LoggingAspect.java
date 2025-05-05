//package com.demo.weatherapi.aspect;
//
//import java.util.Arrays;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Aspect
//@Component
//public class LoggingAspect {
//
//    @Pointcut("execution(* com.demo.weatherapi.controller..*(..))")
//    public void controllerMethods() {}
//
//    @Before("controllerMethods()")
//    public void logBefore(JoinPoint joinPoint) {
//        log.info("Вызов метода: {} с аргументами: {}",
//                joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
//    }
//
//    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
//    public void logAfter(JoinPoint joinPoint, Object result) {
//        log.info("Метод завершен: {} с результатом: {}", joinPoint.getSignature(), result);
//    }
//
//    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
//    public void logException(JoinPoint joinPoint, Throwable ex) {
//        log.error("Ошибка в методе: {}. Сообщение: {}",
//                joinPoint.getSignature(), ex.getMessage(), ex);
//    }
//}
//
//
