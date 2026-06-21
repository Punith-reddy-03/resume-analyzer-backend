package com.resumeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResumeAnalyzerApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ResumeAnalyzerApplication.class, args);
        } catch (Exception e) {
            System.err.println("=========================================");
            System.err.println("❌ APPLICATION FAILED TO START!");
            System.err.println("=========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println("=========================================");
            e.printStackTrace();
        }
    }
}