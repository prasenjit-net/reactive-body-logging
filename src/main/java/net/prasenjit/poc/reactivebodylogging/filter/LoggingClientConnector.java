package net.prasenjit.poc.reactivebodylogging.filter;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.*;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Log4j2
public class LoggingClientConnector implements ClientHttpConnector {
    private final ClientHttpConnector delegate;

    public LoggingClientConnector(ClientHttpConnector delegate) {
        this.delegate = delegate;
    }

    @Override
    @NonNull
    public Mono<ClientHttpResponse> connect(@NonNull HttpMethod method, @NonNull URI uri, @NonNull Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
        return delegate.connect(method, uri, request -> requestCallback.apply(new LoggingClientHttpRequest(request))).map(LoggingClientHttpResponse::new);
    }

    private static class LoggingClientHttpRequest extends ClientHttpRequestDecorator {
        public LoggingClientHttpRequest(ClientHttpRequest delegate) {
            super(delegate);
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            Flux<? extends DataBuffer> buffer = Flux.from(body).doOnNext(dataBuffer -> {
                String bodyString = dataBuffer.toString(StandardCharsets.UTF_8);
                log.info("Request body: {}", bodyString);
            });

            return super.writeWith(buffer);
        }

    }

    private static class LoggingClientHttpResponse extends ClientHttpResponseDecorator {
        public LoggingClientHttpResponse(ClientHttpResponse delegate) {
            super(delegate);
        }

        @Override
        @NonNull
        public Flux<DataBuffer> getBody() {
            return super.getBody()
                    .doOnNext(dataBuffer -> {
                        String bodyString = dataBuffer.toString(StandardCharsets.UTF_8);
                        log.info("Response body: {}", bodyString);
                    });
        }
    }
}
