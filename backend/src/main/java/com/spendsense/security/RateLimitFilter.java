package com.spendsense.security;


import com.spendsense.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter{
    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException{
        String uri=request.getRequestURI();
        if(uri.equals("/api/v1/actuator/health") || uri.startsWith("/api/v1/webhooks")){
            chain.doFilter(request, response);
            return;
        }

        String clientId=getClientIdentifier(request);
        Bucket bucket=rateLimitConfig.resolveBucket(clientId);
        if(bucket.tryConsume(1)){
            response.setHeader("X-RateLimit-Remaining",
            String.valueOf(bucket.getAvailableTokens()));
            chain.doFilter(request, response);
        }
        else{
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}"
            );
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String ip=request.getHeader("X-Forwarded-For");
        if(ip==null || ip.isEmpty()){
            ip=request.getRemoteAddr();
        }
        return "ip:"+ip;
    }

}
