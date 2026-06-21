package com.resumeanalyzer.service;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.ai.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    public Resume saveResume(Resume resume) {
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        return resumeRepository.save(resume);
    }
    
    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }
    
    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }
    
    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }
    
    public Resume updateResume(Resume resume) {
        resume.setUpdatedAt(LocalDateTime.now());
        return resumeRepository.save(resume);
    }
    
    public String analyzeResumeContent(Long id) {
        Optional<Resume> resumeOpt = resumeRepository.findById(id);
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            String analysis = geminiService.analyzeResume(resume.getFileContent());
            resume.setAnalysisResult(analysis);
            resume.setUpdatedAt(LocalDateTime.now());
            resumeRepository.save(resume);
            return analysis;
        }
        throw new RuntimeException("Resume not found with id: " + id);
    }
}
