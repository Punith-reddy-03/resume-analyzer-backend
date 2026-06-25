package com.resumeanalyzer.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    // ✅ Using gemini-1.5-flash
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    
    public String analyzeResume(String resumeContent) {
        try {
            String url = GEMINI_URL + apiKey;
            
            System.out.println("📡 Calling Gemini API with model: gemini-1.5-flash");
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            
            String prompt = "You are an expert resume analyzer. Analyze this resume and provide:\n" +
                           "1. Strengths (3-4 points)\n" +
                           "2. Areas for improvement (3-4 points)\n" +
                           "3. Overall score out of 100\n" +
                           "4. Suggestions for improvement\n\n" +
                           "Resume: " + resumeContent;
            
            textPart.put("text", prompt);
            parts.add(textPart);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List candidates = (List) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map firstCandidate = (Map) candidates.get(0);
                    Map contentResponse = (Map) firstCandidate.get("content");
                    List partsResponse = (List) contentResponse.get("parts");
                    Map firstPart = (Map) partsResponse.get(0);
                    String result = (String) firstPart.get("text");
                    System.out.println("✅ Analysis completed successfully!");
                    return result;
                }
            }
            
            return "No analysis generated. Please check your API key.";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing resume: " + e.getMessage();
        }
    }
}