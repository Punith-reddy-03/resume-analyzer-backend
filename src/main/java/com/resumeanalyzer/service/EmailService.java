package com.resumeanalyzer.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public String sendEmail(String toEmail, String subject, String message) {
        try {
            // For testing, just log the email
            System.out.println("📧 Sending email to: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Message: " + message);
            
            // In production, you can integrate with EmailJS or SendGrid
            // For now, return success
            return "✅ Email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "❌ Error sending email: " + e.getMessage();
        }
    }
}