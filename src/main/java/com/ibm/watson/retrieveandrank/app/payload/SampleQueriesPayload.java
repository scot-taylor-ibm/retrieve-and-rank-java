package com.ibm.watson.retrieveandrank.app.payload;

import java.util.List;

import com.google.common.collect.Lists;

public class SampleQueriesPayload {
    private SampleQueryPayload[] queries;
    private List<SampleQueryPayload> queryList;

    public SampleQueryPayload[] getQueries() {
        if(queries == null && queryList != null){
            queries = queryList.toArray(new SampleQueryPayload[0]);
        }
        return queries;
    }

    public void setQueries(SampleQueryPayload[] queries) {
        this.queries = queries;
    }
    
    public void addQuery(SampleQueryPayload query){
        if(queryList == null){
            queryList = Lists.newArrayList();
        }
        queryList.add(query);
    }

}
