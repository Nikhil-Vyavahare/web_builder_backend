package com.example.gemini.gemini.Entity;

import lombok.Data;

@Data
public class GeneratedCode {

    private String prompt;
    
    private String code;

    private String type; // html, css, js

    public GeneratedCode() {}

    public GeneratedCode(String prompt, String code, String type) {
        this.prompt = prompt;
        this.code = code;
        this.type = type;
    }

  
    
}