package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import com.resumeanalyzer.ai.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeController {
    
    @Autowired
    private ResumeService resumeService;
    
    @Autowired
    private GeminiService geminiService;
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            String content = "";
            String fileName = file.getOriginalFilename();
            System.out.println("📄 Processing file: " + fileName);
            
            // Extract text based on file type
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                // Extract text from PDF using PDFBox 3.0.0
                // Loader.loadPDF expects byte[], so we use file.getBytes()
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    content = stripper.getText(document);
                    System.out.println("📄 Extracted " + content.length() + " characters from PDF");
                }
            } else if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
                // Extract text from DOCX
                try (InputStream inputStream = file.getInputStream();
                     XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    content = extractor.getText();
                    System.out.println("📄 Extracted " + content.length() + " characters from DOCX");
                }
            } else if (fileName != null && fileName.toLowerCase().endsWith(".txt")) {
                // Read text file directly
                content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("📄 Read " + content.length() + " characters from TXT");
            } else {
                return ResponseEntity.status(400).body("Unsupported file format. Please upload PDF, DOCX, or TXT.");
            }
            
            // Clean the content - remove null characters and control characters
            content = content.replaceAll("\u0000", "");
            content = content.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
            content = content.trim();
            
            // If content is empty, return error
            if (content.isEmpty() || content.length() < 10) {
                return ResponseEntity.status(400).body("Could not extract text from the file. Please make sure the file contains readable text.");
            }
            
            System.out.println("📄 Final content length: " + content.length());
            
            // Create a new resume
            Resume resume = new Resume();
            resume.setFileName(fileName);
            resume.setFileContent(content);
            
            // Save to database
            Resume savedResume = resumeService.saveResume(resume);
            
            // Analyze with Gemini
            String analysis = geminiService.analyzeResume(content);
            savedResume.setAnalysisResult(analysis);
            
            // Extract score from analysis
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