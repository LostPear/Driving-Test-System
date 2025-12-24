package com.exam.model;

import java.util.List;

public class Question {
    private int id;
    private String type; // single, judge, multiple
    private String difficulty; // easy, medium, hard
    private String question;
    private String image; // 图片路径
    private List<String> options;
    private int correctAnswer; // 向后兼容，用于single和judge
    private List<Integer> correctAnswers; // 用于multiple类型，存储多个正确答案
    private String explanation;
    private String createdAt;
    
    public Question() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public List<Integer> getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(List<Integer> correctAnswers) { this.correctAnswers = correctAnswers; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

