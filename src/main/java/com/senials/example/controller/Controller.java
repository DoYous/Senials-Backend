package com.senials.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class Controller {

    @GetMapping("/api/hello")
    public List<String> Hello() {
        return Arrays.asList("서버서버", "뷰뷰");
    }
}
