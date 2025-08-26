package com.example.gemini.gemini.service;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.gemini.gemini.Entity.GeneratedCode;

import okhttp3.*;

@Service
public class GeminiService {

    // Removed the GeneratedCodeRepository dependency as we are no longer saving to a database.
    // private final GeneratedCodeRepository repo;

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.key2}")
    private String apiKey2;

    // We've removed the constructor that injected the repository.
    // public GeminiService(GeneratedCodeRepository repo) {
    //     this.repo = repo;
    // }


    // Changed the return type from GeneratedCode to String, as we no longer save and return the entity.
    public GeneratedCode callGeminiAPI(String userPrompt) {
        OkHttpClient client = new OkHttpClient();
        try {
            // Full prompt for HTML generation.
            String fullPrompt = "Generate a complete HTML page with inline CSS and JS. " +
                    "Do not provide separate CSS or JS files. " + "Do not explain anything just give"+
                    "Only HTML content inside <html> tags. \n\nUser request: " + userPrompt;

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("parts", new JSONArray().put(new JSONObject().put("text", fullPrompt)));
            contents.put(user);
            requestBody.put("contents", contents);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string(); // read first

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Gemini API Error: HTTP " + response.code() + ", Body: " + responseBody
                );
            }

            JSONObject json = new JSONObject(responseBody);
            String generatedCode = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Removed the database save operation: repo.save(codeEntity);
             GeneratedCode codeEntity = new GeneratedCode(userPrompt, generatedCode, "html");
            // Return the generated code directly.
            return codeEntity;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate UI: " + e.getMessage(), e);
        }
    }

    public String enhancePrompt(String userPrompt) {
        OkHttpClient client = new OkHttpClient();
        try {
            String fullPrompt =
                "Rewrite the following user prompt into a single, clear, and professional instruction " +
                "for generating a modern, responsive, and visually polished HTML UI. " +
                "The UI should follow best design practices, be production-ready, and use clean, semantic HTML, " +
                "CSS (or inline styles), and JavaScript. " +
                "Do not provide multiple options. Do not explain. " +
                "Only return the enhanced prompt as plain text.\n\n"
                + userPrompt;

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("parts", new JSONArray().put(new JSONObject().put("text", fullPrompt)));
            contents.put(user);
            requestBody.put("contents", contents);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey2)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Gemini API Error (enhancePrompt): HTTP " + response.code() + ", Body: " + responseBody
                );
            }

            JSONObject json = new JSONObject(responseBody);
            return json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            throw new RuntimeException("Failed to enhance prompt: " + e.getMessage(), e);
        }
    }
}

