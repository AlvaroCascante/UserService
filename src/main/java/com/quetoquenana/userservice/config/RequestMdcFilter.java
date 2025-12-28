package com.quetoquenana.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.quetoquenana.userservice.util.Constants.Logging.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestMdcFilter extends OncePerRequestFilter {

    private final Environment env;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {
        String appEnv = env.getProperty(ENV_PROPERTY, ENV_PROPERTY_DEFAULT);
        MDC.put(MDC_ENV_KEY, appEnv);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_ENV_KEY);
        }
    }
}