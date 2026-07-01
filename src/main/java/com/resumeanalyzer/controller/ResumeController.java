package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import com.resumeanalyzer.service.EmailService;
import com.resumeanalyzer.ai.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {
    
    @Autowired
    private ResumeService resumeService;
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private EmailService emailService;
    
    // ✅ Test Hugging Face connectivity
    @GetMapping("/test-huggingface")
    public ResponseEntity<String> testHuggingFace() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", "Hello, who are you?");
            requestBody.put("parameters", Map.of("max_new_tokens", 20));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api-inference.huggingface.co/models/google/flan-t5-base",
                entity,
                String.class
            );
            
            return ResponseEntity.ok("✅ Hugging Face is reachable! Response: " + response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
    
    // ✅ Send Email Notification
    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestParam("toEmail") String toEmail,
                                            @RequestParam("score") int score,
                                            @RequestParam("fileName") String fileName,
                                            @RequestParam("status") String status) {
        try {
            String subject;
            String message;
            
            if (score >= 70) {
                subject = "🎉 Congratulations! Your Resume is Impressive!";
                message = "Dear Candidate,\n\n" +
                         "Your resume \"" + fileName + "\" has been analyzed and scored " + score + "/100.\n\n" +
                         "✅ You are capable for the job role!\n" +
                         "✅ Your skills and experience match the requirements.\n" +
                         "✅ You have strong potential for this position.\n\n" +
                         "📋 Key Strengths identified:\n" +
                         "• Strong technical skills\n" +
                         "• Relevant experience\n" +
                         "• Good academic background\n\n" +
                         "We recommend you to apply for the position with confidence.\n\n" +
                         "Best regards,\n" +
                         "AI Resume Analyzer Team";
            } else {
                subject = "📈 Resume Improvement Suggestions";
                message = "Dear Candidate,\n\n" +
                         "Your resume \"" + fileName + "\" has been analyzed and scored " + score + "/100.\n\n" +
                         "⚠️ Your resume needs improvement to meet job requirements.\n\n" +
                         "📋 Suggestions for improvement:\n" +
                         "• Add more quantifiable achievements\n" +
                         "• Include relevant work experience\n" +
                         "• Highlight specific technical skills\n" +
                         "• Improve resume formatting\n" +
                         "• Add a professional summary\n\n" +
                         "Keep working on your resume and try again!\n\n" +
                         "Best regards,\n" +
                         "AI Resume Analyzer Team";
            }
            
            String result = emailService.sendEmail(toEmail, subject, message);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error sending email: " + e.getMessage());
        }
    }
    
    // ✅ Get Analysis History
    @GetMapping("/history")
    public ResponseEntity<List<Resume>> getHistory() {
        try {
            List<Resume> history = resumeService.getAllResumes();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    // ✅ Compare Resumes
    @PostMapping("/compare")
    public ResponseEntity<?> compareResumes(@RequestParam("file1") MultipartFile file1,
                                            @RequestParam("file2") MultipartFile file2) {
        try {
            String content1 = extractText(file1);
            String content2 = extractText(file2);
            
            String analysis1 = geminiService.analyzeResume(content1);
            String analysis2 = geminiService.analyzeResume(content2);
            
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("file1Name", file1.getOriginalFilename());
            comparison.put("file2Name", file2.getOriginalFilename());
            comparison.put("analysis1", analysis1);
            comparison.put("analysis2", analysis2);
            
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error comparing resumes: " + e.getMessage());
        }
    }
    
    // ✅ Job Description Matching
    @PostMapping("/match-job")
    public ResponseEntity<?> matchJobDescription(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("jobDescription") String jobDescription) {
        try {
            String content = extractText(file);
            
            String prompt = "You are an expert resume analyzer. Compare the following resume with the job description and provide:\n" +
                           "1. Match Score (0-100%)\n" +
                           "2. Matching Skills (list)\n" +
                           "3. Missing Skills (list)\n" +
                           "4. Overall Recommendation\n\n" +
                           "Resume: " + content + "\n\n" +
                           "Job Description: " + jobDescription;
            
            String analysis = geminiService.analyzeResume(prompt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("jobDescription", jobDescription);
            result.put("analysis", analysis);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error matching job description: " + e.getMessage());
        }
    }
    
    private String extractText(MultipartFile file) throws Exception {
        String content = "";
        String fileName = file.getOriginalFilename();
        
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                content = stripper.getText(document);
            }
        } else if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            try (InputStream inputStream = file.getInputStream();
                 XWPFDocument document = new XWPFDocument(inputStream);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                content = extractor.getText();
            }
        } else if (fileName != null && fileName.toLowerCase().endsWith(".txt")) {
            content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        
        content = content.replaceAll("\u0000", "");
        content = content.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
        content = content.trim();
        
        return content;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            String content = extractText(file);
            String fileName = file.getOriginalFilename();
            
            System.out.println("📄 Processing file: " + fileName);
            System.out.println("📄 Extracted " + content.length() + " characters");
            
            if (content.isEmpty() || content.length() < 10) {
                return ResponseEntity.status(400).body("Could not extract text from the file. Please make sure the file contains readable text.");
            }
            
            Resume resume = new Resume();
            resume.setFileName(fileName);
            resume.setFileContent(content);
            
            Resume savedResume = resumeService.saveResume(resume);
            
            String analysis = geminiService.analyzeResume(content);
            savedResume.setAnalysisResult(analysis);
            
            double score = extractScore(analysis);
            savedResume.setOverallScore(score);
            resumeService.updateResume(savedResume);
            
            return ResponseEntity.ok(savedResume);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error analyzing resume: " + e.getMessage());
        }
    }
    
    private double extractScore(String analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return 70.0;
        }
        
        try {
            String[] scorePatterns = {"Overall score", "score out of 100", "Score:", "Rating:"};
            for (String pattern : scorePatterns) {
                if (analysis.contains(pattern)) {
                    String[] parts = analysis.split(pattern);
                    if (parts.length > 1) {
                        String scoreStr = parts[1].replaceAll("[^0-9.]", "");
                        if (!scoreStr.isEmpty()) {
                            double foundScore = Double.parseDouble(scoreStr);
                            if (foundScore >= 0 && foundScore <= 100) {
                                return foundScore;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Keep default score
        }
        return 70.0;
    }
    
    @PostMapping
    public Resume createResume(@RequestBody Resume resume) {
        return resumeService.saveResume(resume);
    }
    
    @GetMapping
    public List<Resume> getAllResumes() {
        return resumeService.getAllResumes();
    }
    
    @GetMapping("/{id}")
    public Resume getResumeById(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + id));
    }
    
    @DeleteMapping("/{id}")
    public String deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return "Resume deleted successfully!";
    }
    
    @PostMapping("/{id}/analyze")
    public String analyzeResumeById(@PathVariable Long id) {
        return resumeService.analyzeResumeContent(id);
    }
}