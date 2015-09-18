package com.ibm.watson.retrieveandrank.app.payload;
/**
 * An outbound payload object that gets returned to the client. The payload describes
 * one of the sample queries as provided by the data set. The queries are stored in the 
 * SampleQueries.json file.
 */
public class SampleQueryPayload {
	//a query string e.g. "What is xyz?"
    private String query;
    //an id that can be used to identify the query for Ground Truth purposes
    private int id;
    //an id that can be used to map the query back to the original data set.
    private int queryId;

    /**
     * Returns the query string
     * @return
     */
    public String getQuery() {
        return query;
    }
    /**
     * Sets the query string
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }
    /**
     * Returns the ID for ground truth lookup
     * @return
     */
    public int getId() {
        return id;
    }
    /**
     * Sets the ground truth ID
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Returns the id which allows lookup of the original data set
     * @return
     */
    public int getQueryId() {
        return queryId;
    }
    /**
     * Sets the original data set id
     * @param queryId
     */
    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

}
