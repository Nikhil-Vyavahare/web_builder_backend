package com.example.gemini.gemini.controller;

import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.gemini.gemini.service.GeminiService;
import com.example.gemini.gemini.Entity.GeneratedCode;


@RestController
@RequestMapping("/api")
public class GeminiController {

    private final GeminiService service;

    public GeminiController(GeminiService service) {
        this.service = service;
    }

    @PostMapping("/generate-ui")
    public ResponseEntity<?> generateUI(@RequestBody UserPrompt prompt) {
        try {
            GeneratedCode code = service.callGeminiAPI(prompt.getUserProblem());
            return ResponseEntity.ok(code);
        } catch (RuntimeException e) {
            // Return a proper 500 with error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate UI: " + e.getMessage()));
        }
    }

    @PostMapping("/enhance-prompt")
    public ResponseEntity<?> enhancePrompt(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt is required");
        }
        try {
            String enhancedPrompt = service.enhancePrompt(prompt);
            return ResponseEntity.ok(Map.of("enhancedPrompt", enhancedPrompt));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to enhance prompt: " + e.getMessage()));
        }
    }
}

// Request model
class UserPrompt {
    private String userProblem;

    public String getUserProblem() {
        return userProblem;
    }

    public void setUserProblem(String userProblem) {
        this.userProblem = userProblem;
    }
}
