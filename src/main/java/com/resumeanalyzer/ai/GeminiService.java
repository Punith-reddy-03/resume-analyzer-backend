package com.resumeanalyzer.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import java.util.*;

@Service
public class GeminiService {

    private final String COHERE_URL = "https://api.cohere.ai/v1/chat";
    
    // ✅ Read API key from environment variable (NOT hardcoded)
    private final String COHERE_API_KEY = System.getenv("COHERE_API_KEY");

    public String analyzeResume(String resumeContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + COHERE_API_KEY);

            String message = "You are an expert resume analyzer. Analyze the following resume and provide a detailed, structured response with clear sections. Use the exact format below:\n\n" +
                             "=== STRENGTHS ===\n" +
                             "List 4-5 specific strengths with brief explanations.\n\n" +
                             "=== AREAS FOR IMPROVEMENT ===\n" +
                             "List 4-5 specific areas that need improvement with brief explanations.\n\n" +
                             "=== OVERALL SCORE ===\n" +
                             "Provide a score out of 100 with a brief justification.\n\n" +
                             "=== DETAILED SUGGESTIONS ===\n" +
                             "Provide 4-5 specific, actionable suggestions to improve the resume.\n\n" +
                             "=== KEY SKILLS IDENTIFIED ===\n" +
                             "List all technical and soft skills found in the resume.\n\n" +
                             "Resume: " + resumeContent;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "command-r-08-2024");
            requestBody.put("message", message);
            requestBody.put("max_tokens", 800);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                COHERE_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("text")) {
                String text = (String) responseBody.get("text");
                System.out.println("✅ Analysis completed!");
                return text.trim();
            }

            return "No analysis generated. Please try again.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing resume: " + e.getMessage();
        }
    }
}