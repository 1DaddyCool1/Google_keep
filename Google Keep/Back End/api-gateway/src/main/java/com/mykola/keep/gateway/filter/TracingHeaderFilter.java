package com.mykola.keep.gateway.filter;

import brave.Tracer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TracingHeaderFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    public TracingHeaderFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            if (tracer.currentSpan() != null) {
                String traceId = tracer.currentSpan().context().traceIdString();
                String spanId = tracer.currentSpan().context().spanIdString();

                exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);
                exchange.getResponse().getHeaders().add("X-Span-Id", spanId);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

