package com.trae.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        log.debug("这是一条DEBUG级别的日志");
        log.info("这是一条INFO级别的日志");
        log.warn("这是一条WARN级别的日志");
        log.error("这是一条ERROR级别的日志");
        return "Welcome to Spring Boot!";
    }
}