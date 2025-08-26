package com.example.gemini.gemini.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.gemini.gemini.Entity.GeneratedCode;

import okhttp3.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.key2}")
    private String apiKey2;

    // Generate HTML code using Gemini 2.5 Flash
    public GeneratedCode callGeminiAPI(String userPrompt) {
        OkHttpClient client = new OkHttpClient();
        try {
            String fullPrompt = "Generate a complete HTML page with inline CSS and JS. " +
                    "Do not provide separate CSS or JS files. Do not explain anything; just give " +
                    "Only HTML content inside <html> tags.\n\nUser request: " + userPrompt;

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("parts", new JSONArray().put(new JSONObject().put("text", fullPrompt)));
            contents.put(user);
            requestBody.put("contents", contents);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

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

            return new GeneratedCode(userPrompt, generatedCode, "html");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate UI: " + e.getMessage(), e);
        }
    }

    // Enhance prompt using Gemini 2.5 Flash
    public String enhancePrompt(String userPrompt) {
        OkHttpClient client = new OkHttpClient();
        try {
            String fullPrompt = "Improve and expand this user prompt for generating a modern, responsive, and clean HTML UI: \n\n"
                    + userPrompt;

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("parts", new JSONArray().put(new JSONObject().put("text", fullPrompt)));
            contents.put(user);
            requestBody.put("contents", contents);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey2)
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

