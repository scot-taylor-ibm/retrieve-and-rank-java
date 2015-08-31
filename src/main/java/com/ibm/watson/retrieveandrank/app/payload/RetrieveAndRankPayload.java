package com.ibm.watson.retrieveandrank.app.payload;

import java.util.List;

public class RetrieveAndRankPayload {
    private String query;
    private int num_solr_results;
    private List<Answer> solr_results;
    private List<Answer> ranked_results;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNum_solr_results() {
        return num_solr_results;
    }

    public void setNum_solr_results(int num_solr_results) {
        this.num_solr_results = num_solr_results;
    }

    public List<Answer> getSolr_results() {
        return solr_results;
    }

    public void setSolr_results(List<Answer> solr_results) {
        this.solr_results = solr_results;
    }

    public List<Answer> getRanked_results() {
        return ranked_results;
    }

    public void setRanked_results(List<Answer> ranked_results) {
        this.ranked_results = ranked_results;
    }
}
