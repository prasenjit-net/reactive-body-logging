package net.prasenjit.poc.reactivebodylogging.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/api")
public class HelloController {

    @PostMapping("/hello")
    public Mono<String> hello(@RequestBody String body) {
        return Mono.just(body + " World");
    }
}
