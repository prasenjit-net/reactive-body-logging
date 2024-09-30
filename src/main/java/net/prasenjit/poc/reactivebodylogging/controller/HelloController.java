package net.prasenjit.poc.reactivebodylogging.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class HelloController {

    @PostMapping("/hello")
    public Mono<String> hello(@RequestBody String body) {
        return Mono.just(body + " World");
    }
}
