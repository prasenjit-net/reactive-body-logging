package net.prasenjit.poc.reactivebodylogging.filter;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        private final HttpContentEvent.HttpContentEventBuilder eventBuilder;

        public LoggingClientHttpRequest(ClientHttpRequest delegate) {
            super(delegate);
            HttpMethod method = delegate.getMethod();
            String uri = delegate.getURI().toString();
            eventBuilder = HttpContentEvent.builder()
                    .method(method)
                    .uri(uri)
                    .headers(delegate.getHeaders());
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            Mono<DataBuffer> dataBufferMono = DataBufferUtils.join(body).doOnNext(dataBuffer -> {
                String bodyString = dataBuffer.toString(StandardCharsets.UTF_8);
                HttpContentEvent event = eventBuilder.body(bodyString).build();
                log.info(event);
            });

            return super.writeWith(dataBufferMono);
        }

    }

    private static class LoggingClientHttpResponse extends ClientHttpResponseDecorator {
        private final HttpContentEvent.HttpContentEventBuilder eventBuilder;

        public LoggingClientHttpResponse(ClientHttpResponse delegate) {
            super(delegate);
            HttpStatusCode statusCode = delegate.getStatusCode();
            eventBuilder = HttpContentEvent.builder()
                    .status(HttpStatus.valueOf(statusCode.value()))
                    .headers(delegate.getHeaders());
        }

        @Override
        @NonNull
        public Flux<DataBuffer> getBody() {
            return DataBufferUtils.join(super.getBody()).doOnNext(dataBuffer -> {
                String bodyString = dataBuffer.toString(StandardCharsets.UTF_8);
                HttpContentEvent event = eventBuilder.body(bodyString).build();
                log.info(event);
            }).flux();
        }
    }
}
