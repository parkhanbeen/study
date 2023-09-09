package com.example.demoproject.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demoproject.web.dto.HelloResponseDto;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/hello/dto")
    public HelloResponseDto hello(
        @RequestParam("name") String name,
        @RequestParam("amount") int amount) {
        return new HelloResponseDto(name, amount);
    }

}
