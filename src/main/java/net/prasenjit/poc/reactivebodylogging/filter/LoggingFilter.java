package net.prasenjit.poc.reactivebodylogging.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange.mutate()
                .request(decorateRequest(exchange.getRequest()))
                .response(decorateResponse(exchange.getResponse()))
                .build());
    }

    private ServerHttpRequest decorateRequest(ServerHttpRequest request) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            @NonNull
            public Flux<DataBuffer> getBody() {
                return super.getBody().map(dataBuffer -> {
                    String body = dataBuffer.toString(StandardCharsets.UTF_8);
                    log.info("Request body: {}", body);
                    return dataBuffer;
                });
            }
        };
    }

    private ServerHttpResponse decorateResponse(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            @NonNull
            public Mono<Void> writeWith(@NonNull org.reactivestreams.Publisher<? extends DataBuffer> body) {
                return super.writeWith(Flux.from(body).map(dataBuffer -> {
                    String bodyString = dataBuffer.toString(StandardCharsets.UTF_8);
                    log.info("Response body: {}", bodyString);
                    return dataBuffer;
                }));
            }
        };
    }
}