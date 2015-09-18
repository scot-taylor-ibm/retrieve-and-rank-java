package com.ibm.watson.retrieveandrank.app.payload;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * An outbound payload object which describes all of the Sample queries as defined in 
 * /SampleQueries.json. The payload is used by the client to display/present a list of 
 * sample queries which the user can use to test the system.
 */
public class SampleQueriesPayload {
	//An array of Queries. A query typically has a 'query text' and 'query id'
    private SampleQueryPayload[] queries;
    //A util list to make it easier to add queries to the payload
    private List<SampleQueryPayload> queryList;

    /**
     * Returnes the list of Queries to the client
     * @return
     */
    public SampleQueryPayload[] getQueries() {
        if(queries == null && queryList != null){
            queries = queryList.toArray(new SampleQueryPayload[0]);
        }
        return queries;
    }
    /**
     * Sets a list of queries which will be returned to the client
     * @param queries
     */
    public void setQueries(SampleQueryPayload[] queries) {
        this.queries = queries;
    }
    /*
     * Util function which allows the code internally to easily add a query to the payload.
     */
    public void addQuery(SampleQueryPayload query){
        if(queryList == null){
            queryList = Lists.newArrayList();
        }
        queryList.add(query);
    }

}
