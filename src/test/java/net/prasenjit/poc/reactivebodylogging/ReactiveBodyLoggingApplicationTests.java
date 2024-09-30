package net.prasenjit.poc.reactivebodylogging;

import net.prasenjit.poc.reactivebodylogging.filter.LoggingClientConnector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReactiveBodyLoggingApplicationTests {

    @LocalServerPort
    int port;

    @Test
    void contextLoads() {
        Mono<String> stringMono = WebClient.builder()
                .clientConnector(new LoggingClientConnector(new ReactorClientHttpConnector()))
                .baseUrl("http://localhost:" + port)
                .build()
                .post()
                .uri("/api/hello")
                .bodyValue("Hello")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(stringMono)
                .expectNext("Hello World")
                .verifyComplete();
    }

}
