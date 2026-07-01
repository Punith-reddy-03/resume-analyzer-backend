package com.resumeanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${email.api.key}")
    private String emailApiKey;

    private final String EMAIL_URL = "https://api.emailjs.com/api/v1.0/email/send";

    public String sendEmail(String toEmail, String subject, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_id", "service_your_service_id");
            requestBody.put("template_id", "template_your_template_id");
            requestBody.put("user_id", emailApiKey);
            
            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("to_email", toEmail);
            templateParams.put("subject", subject);
            templateParams.put("message", message);
            requestBody.put("template_params", templateParams);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                EMAIL_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            return "✅ Email sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error sending email: " + e.getMessage();
        }
    }
}