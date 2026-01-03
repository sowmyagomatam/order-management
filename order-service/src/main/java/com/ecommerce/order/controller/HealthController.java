package com.ecommerce.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @Value("${spring.application.name}")
    String serviceName;

    @GetMapping
    ResponseEntity<Map<String, Object>> health(){
        Map<String, Object> response = Map.of("service", serviceName,
                "status", "UP",
                "timestamp", Instant.now());
        return ResponseEntity.ok(response);
    }
}
