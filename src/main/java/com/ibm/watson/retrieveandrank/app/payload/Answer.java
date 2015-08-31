package com.ibm.watson.retrieveandrank.app.payload;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Answer {
    private String answerId;
    private float score;
    private double confidence;
    private String title;
    private String body;
    private int solrRank;
    private int finalRank;
    private int relevance = -1;
    private static Pattern p = Pattern.compile("(.{55,85}\\W)");
    
    public String getAnswerId() {
        return answerId;
    }
    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }
    public double getConfidence() {
        return confidence;
    }
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public int getSolrRank() {
        if(solrRank > -1){
            solrRank++;
        }
        return solrRank;
    }
    public void setSolrRank(int solrRank) {
        this.solrRank = solrRank;
    }
    public int getFinalRank() {
        return finalRank;
    }
    public void setFinalRank(int finalRank) {
        if(finalRank > -1){
            finalRank++;
        }
        this.finalRank = finalRank;
    }
    public int getRelevance() {
        return relevance;
    }
    public void setRelevance(int relevance) {
        if(relevance < 1){
            relevance = 0;
        }
        else if(relevance == 1){
            relevance = 4;
        }
        else if(relevance == 2){
            relevance = 3;
        }
        else if(relevance == 3){
            relevance = 2;
        }
        else if(relevance == 4){
            relevance = 1;
        }
        this.relevance = relevance;
    }
}
