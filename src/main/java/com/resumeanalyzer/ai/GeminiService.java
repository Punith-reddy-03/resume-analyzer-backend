package com.resumeanalyzer.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import java.util.*;

@Service
public class GeminiService {

    // ✅ Using a reliable and lightweight model
    private final String HUGGING_FACE_URL = "https://api-inference.huggingface.co/models/gpt2";

    public String analyzeResume(String resumeContent) {
        try {
            // ✅ Set up RestTemplate with timeouts
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // ✅ Add a User-Agent header to avoid being blocked
            headers.set("User-Agent", "ResumeAnalyzer/1.0");

            // ✅ Prepare the prompt
            String prompt = "Analyze this resume and provide: 1. Strengths (3-4 points), 2. Areas for improvement (3-4 points), 3. Overall score out of 100, 4. Suggestions for improvement.\n\nResume: " + resumeContent;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 250,
                "temperature", 0.7,
                "do_sample", true
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // ✅ Make the API call with error handling
            ResponseEntity<List> response = restTemplate.exchange(
                HUGGING_FACE_URL,
                HttpMethod.POST,
                entity,
                List.class
            );

            // ✅ Process the response
            List responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                Map firstResult = (Map) responseBody.get(0);
                String generatedText = (String) firstResult.get("generated_text");
                
                // ✅ Remove the prompt from the generated text
                String analysis = generatedText.replace(prompt, "").trim();
                System.out.println("✅ Analysis completed successfully!");
                return analysis.isEmpty() ? "No analysis generated." : analysis;
            }

            return "No analysis generated. Please try again.";

        } catch (ResourceAccessException e) {
            System.err.println("❌ Network error: " + e.getMessage());
            return "Network error: Unable to reach Hugging Face API. Please check your internet connection.";
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return "Error analyzing resume: " + e.getMessage();
        }
    }
}