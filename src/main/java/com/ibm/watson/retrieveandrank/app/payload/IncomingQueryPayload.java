package com.ibm.watson.retrieveandrank.app.payload;

/**
 * A payload object used to describe an incoming query from the client.
 *
 */
public class IncomingQueryPayload {
    private String query;
    private int queryId = -1;

    /**
     * Returns the query which is to be sent to the WDS service
     * @return
     */
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    /**
     * Returns the ID of the query to be sent to the service. 
     * @return  returns -1 if the query is not one of the canned queries from the data set
     */
    public int getQueryId() {
        return queryId;
    }

    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

}
