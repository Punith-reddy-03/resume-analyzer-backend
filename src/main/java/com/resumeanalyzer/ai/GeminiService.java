package com.resumeanalyzer.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import java.util.*;

@Service
public class GeminiService {

    // ✅ Using Cohere API (Free, reliable, works on Render)
    private final String COHERE_URL = "https://api.cohere.ai/v1/generate";
    private final String COHERE_API_KEY = "PVuiA9dqODLEgDAvjvqUDuB29Lw5Zn4E45o4QPLQ";

    public String analyzeResume(String resumeContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + COHERE_API_KEY);

            String prompt = "Analyze this resume and provide:\n" +
                            "1. Strengths (3-4 points)\n" +
                            "2. Areas for improvement (3-4 points)\n" +
                            "3. Overall score out of 100\n" +
                            "4. Suggestions for improvement\n\n" +
                            "Resume: " + resumeContent;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "command-r-08-2024");
            requestBody.put("prompt", prompt);
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                COHERE_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("generations")) {
                List generations = (List) responseBody.get("generations");
                if (!generations.isEmpty()) {
                    Map firstGen = (Map) generations.get(0);
                    String text = (String) firstGen.get("text");
                    System.out.println("✅ Analysis completed!");
                    return text.trim();
                }
            }

            return "No analysis generated. Please try again.";

        } catch (ResourceAccessException e) {
            System.err.println("❌ Network error: " + e.getMessage());
            return "Network error: Unable to reach Cohere API. Please check your internet connection.";
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return "Error analyzing resume: " + e.getMessage();
        }
    }
}