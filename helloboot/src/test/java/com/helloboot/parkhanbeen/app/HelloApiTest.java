package com.helloboot.parkhanbeen.app;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HelloApiTest {
    @Test
    void helloApi() {
        // http:localhost:8080/hello?name=hanbeen
        TestRestTemplate rest = new TestRestTemplate();

        ResponseEntity<String> res = rest
            .getForEntity("http://localhost:9090/app/hello?name={name}", String.class, "HanBeen");

        // status 200
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // header(content-type) text/plain
        assertThat(res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.TEXT_PLAIN_VALUE);
        // hello HanBeen
        assertThat(res.getBody()).isEqualTo("*Hello HanBeen*");

    }

    @Test
    void fails_helloApi() {
        // http:localhost:8080/hello?name=hanbeen
        TestRestTemplate rest = new TestRestTemplate();

        ResponseEntity<String> res = rest
            .getForEntity("http://localhost:9090/app/hello?name={name}", String.class, "");

        // status 200
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
