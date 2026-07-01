package com.resumeanalyzer.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {
    
    // ✅ Using a stable and widely available Hugging Face model
    private final String HUGGING_FACE_URL = "https://api-inference.huggingface.co/models/google/flan-t5-base";
    
    public String analyzeResume(String resumeContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prompt engineered for the Flan-T5 model
            String prompt = "Analyze this resume and provide: 1. Strengths (3-4 points), 2. Areas for improvement (3-4 points), 3. Overall score out of 100, 4. Suggestions for improvement.\n\nResume: " + resumeContent;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of("max_new_tokens", 250));
            
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