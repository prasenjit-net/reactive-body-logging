package net.prasenjit.poc.reactivebodylogging.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

@Getter
@Setter
@Builder
@ToString
public class HttpContentEvent {
    private HttpMethod method;
    private HttpStatus status;
    private String uri;
    private MultiValueMap<String, String> headers;
    private String body;
}
