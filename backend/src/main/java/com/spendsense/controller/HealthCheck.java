package com.spendsense.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Health check APIs")
public class HealthCheck {
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the application is running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    public String healthCheck() {
        return "All Good's";
    }
}
