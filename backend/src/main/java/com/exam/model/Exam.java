package com.exam.model;

import java.util.List;
import java.util.Map;

public class Exam {
    private int id;
    private int userId;
    private String type; // 'practice' or 'exam'
    private List<Question> questions;
    private Map<Integer, Integer> answers; // questionId -> answer
    private Integer score;
    private boolean passed;
    private String createdAt;
    private String submittedAt;
    
    public Exam() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    
    public Map<Integer, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<Integer, Integer> answers) { this.answers = answers; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
}

