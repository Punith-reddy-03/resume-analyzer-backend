package com.resumeanalyzer.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {
    
    // ✅ FREE Hugging Face API - Higher limits than Gemini!
    private final String HUGGING_FACE_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.1";
    
    public String analyzeResume(String resumeContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String prompt = "You are an expert resume analyzer. Analyze this resume and provide:\n" +
                           "1. Strengths (3-4 points)\n" +
                           "2. Areas for improvement (3-4 points)\n" +
                           "3. Overall score out of 100\n" +
                           "4. Suggestions for improvement\n\n" +
                           "Resume: " + resumeContent;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of("max_new_tokens", 500));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<List> response = restTemplate.exchange(
                HUGGING_FACE_URL,
                HttpMethod.POST,
                entity,
                List.class
            );
            
            List responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                Map firstResult = (Map) responseBody.get(0);
                String generatedText = (String) firstResult.get("generated_text");
                String analysis = generatedText.replace(prompt, "").trim();
                System.out.println("✅ Analysis completed!");
                return analysis;
            }
            
            return "No analysis generated. Please try again.";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing resume: " + e.getMessage();
        }
    }
}