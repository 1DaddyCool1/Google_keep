package com.mykola.keep.auth.filter;

import brave.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TracingHeaderFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TracingHeaderFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceIdString();
            String spanId = tracer.currentSpan().context().spanIdString();

            response.setHeader("X-Trace-Id", traceId);
            response.setHeader("X-Span-Id", spanId);
        }

        filterChain.doFilter(request, response);
    }
}
