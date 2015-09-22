package com.ibm.watson.retrieveandrank.app.payload;

import java.util.List;

/**
 * The payload which is returned to the client. It contains four properties. The original query sent from the
 * client, the number of results which the Solr portion of the service returned, the list of results as returned
 * by Solr and finally the list of results from Solr re-ranked using the ranker portion of the service.
 */
public class RetrieveAndRankPayload {
	//Original text from the client
    private String query;
    //number of results returned by Solr
    private int num_solr_results;
    //an ordered list of results from the Solr query.
    private List<RankResultPayload> solr_results;
    //an ordered list of results as re-ranked by the Ranker
    private List<RankResultPayload> ranked_results;

    /**
     * Returns the original query sent from the client
     * @return
     */
    public String getQuery() {
        return query;
    }
    /**
     * Set the original query sent from the client
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }
    /**
     * Returns the number of results returned by the Solr search
     * @return
     */
    public int getNum_solr_results() {
        return num_solr_results;
    }
    /**
     * Set the number of results returned by Solr
     * @param num_solr_results
     */
    public void setNum_solr_results(int num_solr_results) {
        this.num_solr_results = num_solr_results;
    }
    /**
     * Returns an ordered list of results as they were returned by the Solr
     * search.
     * @return
     */
    public List<RankResultPayload> getSolr_results() {
        return solr_results;
    }
    /**
     * Sets an ordered list of results as they were returned by Solr
     * @param solr_results
     */
    public void setSolr_results(List<RankResultPayload> solr_results) {
        this.solr_results = solr_results;
    }
    /**
     * Returns an ordered list of results, which is the Solr list re-ranked
     * by the Ranker service
     * @return
     */
    public List<RankResultPayload> getRanked_results() {
        return ranked_results;
    }
    /**
     * Sets an ordered list of results as ranked by the Ranker service.
     * @param ranked_results
     */
    public void setRanked_results(List<RankResultPayload> ranked_results) {
        this.ranked_results = ranked_results;
    }
}
