package com.demo.weatherapi.interceptors;

import com.demo.weatherapi.service.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class VisitCounterInterceptor implements HandlerInterceptor {

    private final VisitCounterService visitCounterService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        String url = request.getRequestURI();
        if (!isTechnicalRequest(url)) {
            String cleanUrl = normalizeUrl(url);
            visitCounterService.incrementVisit(cleanUrl);
        }
    }

    private boolean isTechnicalRequest(String url) {
        return url.contains("api-docs") ||
                url.contains("swagger") ||
                url.contains("${") ||
                url.contains("%") ||
                url.startsWith("/error") ||
                url.startsWith("/actuator");
    }

    private String normalizeUrl(String url) {
        return url.split("\\?")[0];
    }
}