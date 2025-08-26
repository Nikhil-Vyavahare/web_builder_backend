package com.example.gemini.gemini.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.gemini.gemini.Entity.GeneratedCode;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.key2}")
    private String apiKey2;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // Gemini may be slow
            .build();

    // Generate HTML code using Gemini 2.5 Flash
    public GeneratedCode callGeminiAPI(String userPrompt) {
        try {
            String fullPrompt = "Generate a complete HTML page with inline CSS and JS. "
                    + "Do not provide separate CSS or JS files. Do not explain anything; just give "
                    + "only HTML content inside <html> tags.\n\nUser request: " + userPrompt;

            JSONObject requestBody = buildRequest(fullPrompt);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    // Instead of throwing, return error wrapped in GeneratedCode
                    return new GeneratedCode(userPrompt,
                            "<!-- Gemini API Error: HTTP " + response.code() + " Body: " + responseBody + " -->",
                            "html");
                }

                JSONObject json = new JSONObject(responseBody);
                if (!json.has("candidates")) {
                    return new GeneratedCode(userPrompt,
                            "<!-- Gemini returned no candidates. Response: " + responseBody + " -->",
                            "html");
                }

                String generatedCode = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                return new GeneratedCode(userPrompt, generatedCode, "html");
            }

        } catch (Exception e) {
            return new GeneratedCode(userPrompt,
                    "<!-- Failed to generate UI: " + e.getMessage() + " -->",
                    "html");
        }
    }

    // Enhance prompt using Gemini 2.5 Flash
    public String enhancePrompt(String userPrompt) {
        try {
            String fullPrompt = "Improve and expand this user prompt for generating a modern, responsive, and clean HTML UI:\n\n"
                    + userPrompt;

            JSONObject requestBody = buildRequest(fullPrompt);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey2)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return "Gemini API Error (enhancePrompt): HTTP " + response.code() + " - " + responseBody;
                }

                JSONObject json = new JSONObject(responseBody);
                if (!json.has("candidates")) {
                    return "Gemini returned no candidates. Response: " + responseBody;
                }

                return json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }

        } catch (IOException e) {
            return "Gemini request failed (I/O error): " + e.getMessage();
        } catch (Exception e) {
            return "Failed to enhance prompt: " + e.getMessage();
        }
    }

    // Helper to build request JSON
    private JSONObject buildRequest(String prompt) {
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
        contents.put(user);
        requestBody.put("contents", contents);
        return requestBody;
    }
}
