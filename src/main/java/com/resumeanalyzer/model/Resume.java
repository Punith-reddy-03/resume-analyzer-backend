package com.resumeanalyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fileName;
    
    @Column(columnDefinition = "TEXT")
    private String fileContent;
    
    @Column(columnDefinition = "TEXT")
    private String analysisResult;
    
    private Double overallScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Resume() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileContent() { return fileContent; }
    public String getAnalysisResult() { return analysisResult; }
    public Double getOverallScore() { return overallScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileContent(String fileContent) { this.fileContent = fileContent; }
    public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}