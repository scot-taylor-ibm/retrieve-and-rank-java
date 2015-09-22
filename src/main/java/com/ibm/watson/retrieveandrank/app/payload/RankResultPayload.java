package com.ibm.watson.retrieveandrank.app.payload;

/**
 * A payload object that is used to deliver result information to the client. 
 */
public class RankResultPayload {
	//the Solr answer id
    private String answerId;
    //the score assigned by Solr/Ranker
    private float score;
    //the confidence assigned by the ranker to the result
    private double confidence;
    //The title of the document returned by Solr
    private String title;
    //the body of the document returned by Solr
    private String body;
    //the index of the result in the solr results
    private int solrRank;
    //the index of the result in the ranked results
    private int finalRank;
    //the Ground Truth relevance.
    private int relevance = -1;
    
    /**
     * Returns the Solr answer id
     * @return
     */
    public String getAnswerId() {
        return answerId;
    }
    /**
     * Sets the Solr answer id
     * @param answerId
     */
    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }
    /**
     * Returns the score assigned by the system to the result
     * @return
     */
    public float getScore() {
        return score;
    }
    /**
     * Sets the score assigned by the system to the result
     * @param score
     */
    public void setScore(float score) {
        this.score = score;
    }
    /**
     * Returns the confidence assigned by the ranker service to the result
     * @return
     */
    public double getConfidence() {
        return confidence;
    }
    /**
     * Sets the confidence assigned by the ranker service to the result
     * @param confidence
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    /**
     * Returns the Solr document title for the result
     * @return
     */
    public String getTitle() {
        return title;
    }
    /**
     * Returns the Solr document title for the result
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * Returns the Solr document body for the result
     * @return
     */
    public String getBody() {
        return body;
    }
    /**
     * Sets the Solr document body for the result
     * @param body
     */
    public void setBody(String body) {
        this.body = body;
    }
    /**
     * Gets the position this result appears in the list of results from Solr
     * @return
     */
    public int getSolrRank() {
        if(solrRank > -1){
            solrRank++;
        }
        return solrRank;
    }
    /**
     * Sets the position this result appears in the list of results from Solr
     * @param solrRank
     */
    public void setSolrRank(int solrRank) {
        this.solrRank = solrRank;
    }
    /**
     * Returns the position of this result in the list of final results from the ranker
     * @return
     */
    public int getFinalRank() {
        return finalRank;
    }
    /**
     * Sets the position of this result in the list of final results from the ranker
     * @param finalRank
     */
    public void setFinalRank(int finalRank) {
        if(finalRank > -1){
            finalRank++;
        }
        this.finalRank = finalRank;
    }
    /**
     * Returns the Ground Truth relevance of this result
     * @return
     */
    public int getRelevance() {
        return relevance;
    }
    /**
     * Sets the Ground Truth relevance of this result
     * @param relevance
     */
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
