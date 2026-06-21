package com.resumeanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    
    @GetMapping("/api/health")
    public String health() {
        return "Server is up and running! 🚀";
    }
    
    @GetMapping("/api/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Application is running!");
        response.put("message", "PostgreSQL connection is working!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}