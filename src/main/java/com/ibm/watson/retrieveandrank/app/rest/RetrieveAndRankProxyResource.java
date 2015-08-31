/* Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.watson.retrieveandrank.app.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.io.LineReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.retrieveandrank.app.payload.Answer;
import com.ibm.watson.retrieveandrank.app.payload.IncomingQueryPayload;
import com.ibm.watson.retrieveandrank.app.payload.RetrieveAndRankPayload;
import com.ibm.watson.retrieveandrank.app.payload.SampleQueriesPayload;
import com.ibm.watson.retrieveandrank.app.payload.SampleQueryPayload;
import com.ibm.watson.retrieveandrank.app.payload.ServerErrorPayload;
import com.ibm.watson.search.client.http.WatsonSolrClient;

/**
 */

@Path("/bluemix")
public class RetrieveAndRankProxyResource {
    private static String R_N_R_BASE_URL;
    private static String cluster_id;
    private static String ranker_id;
    private static String username = null;
    private static String password = null;
    private static WatsonSolrClient solrClient;
    private static final String FIELD_LIST_PARAM = "fl";
    private static final String FCSELECT_REQUEST_HANDLER = "/fcselect";
    private static final String ID_FIELD = "id";
    private static final String FEATURE_VECTOR_FIELD = "featureVector";
    private static final String SCORE_FIELD = "score";
    private static final String SCORE_HEADER = "score";
    private static final String ANSWER_ID_HEADER = "answer_id";
    private static final String FEATURE_VECTOR_DELIM = " ";
    private static final Character CSV_DELIM = ',';
    private final static String SOLR_CLUSTER_PATH = "/solr_clusters/";
    private static String COLLECTION_NAME = "COLLECTION_NAME";
    private static String rankerURL = "";
    private static JsonObject groundTruth;

    static {
        loadStaticBluemixProperties();
    }

    /**
     * 
     */
    private static void loadStaticBluemixProperties() {
        String envServices = System.getenv("VCAP_SERVICES"); //$NON-NLS-1$
        if (envServices != null) {
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.VCAP_SERVICES_ENV_VAR_FOUND")); //$NON-NLS-1$
            JsonObject services = new JsonParser().parse(envServices).getAsJsonObject();
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.VCAP_SERVICES_JSONOBJECT_SUCCESS")); //$NON-NLS-1$
            JsonArray arr = (JsonArray) services.get("retrieve_and_rank"); //$NON-NLS-1$
            if (arr.size() > 0) {
                services = arr.get(0).getAsJsonObject();
                JsonObject credentials = services.get("credentials").getAsJsonObject(); //$NON-NLS-1$
                R_N_R_BASE_URL = credentials.get("url").getAsString(); //$NON-NLS-1$
                R_N_R_BASE_URL = R_N_R_BASE_URL + "/v1";
                if (credentials.get("username") != null && !credentials.get("username").isJsonNull()) { //$NON-NLS-1$ //$NON-NLS-2$
                    username = credentials.get("username").getAsString(); //$NON-NLS-1$
                    UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.FOUND_USERNAME")); //$NON-NLS-1$
                }
                if (credentials.get("password") != null && !credentials.get("password").isJsonNull()) { //$NON-NLS-1$ //$NON-NLS-2$
                    password = credentials.get("password").getAsString(); //$NON-NLS-1$
                    UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.FOUND_PASSWORD")); //$NON-NLS-1$
                }
            }
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.VCAP_SERVICES_CANNOT_LOAD")); //$NON-NLS-1$
        }

        envServices = System.getenv("CLUSTER_ID"); //$NON-NLS-1$
        if (envServices != null) {
            cluster_id = envServices;
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.RNR_CLUSTER_ID_SUCCESS")); //$NON-NLS-1$
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.RNR_CLUSTER_ID_FAIL")); //$NON-NLS-1$
        }

        envServices = System.getenv("RANKER_ID"); //$NON-NLS-1$
        if (envServices != null) {
            ranker_id = envServices;
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.RNR_RANKER_ID_SUCCESS")); //$NON-NLS-1$
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.RNR_RANKER_ID_FAIL")); //$NON-NLS-1$
        }
        rankerURL = R_N_R_BASE_URL + "/rankers/" + ranker_id;
        URI solrUri = null;
        try {
            solrUri = new URI(R_N_R_BASE_URL + SOLR_CLUSTER_PATH + cluster_id);
            solrClient = new WatsonSolrClient(solrUri, username, password);
        } catch (URISyntaxException e) {
            UtilityFunctions.logger.error(MessageFormat.format(Messages.getString("RetrieveAndRankProxyResource.RNR_SOLR_CLIENT_FAIL"), R_N_R_BASE_URL + SOLR_CLUSTER_PATH + cluster_id)); //$NON-NLS-1$
        }
        
        envServices = System.getenv("COLLECTION_NAME"); //$NON-NLS-1$
        if (envServices != null) {
            COLLECTION_NAME = envServices;
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.RNR_COLLECTION_NAME_SUCCESS")); //$NON-NLS-1$
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.RNR_COLLECTION_NAME_FAIL")); //$NON-NLS-1$
        }
        
        InputStream samples = RetrieveAndRankProxyResource.class.getResourceAsStream("/GroundTruth.json");
        groundTruth = new JsonParser().parse(new InputStreamReader(samples)).getAsJsonObject();
        System.out.println(groundTruth.toString());
    }
 
    /**
     * Gets a list of queries from the resource file on disk. The queries are provided by the Cranfield dataset.. This API randomly selects 
     * n queries (by default 8) and returns those to the client.
     * @param numQueries  the number of queries the client wishes to retrieve, by default 8
     * @return  a list of n queries. The returned query contains a query string and and query ID for ground truth lookup.
     */
    @Path("/sampleQueries")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSampleQueries(@QueryParam("numQueries") @DefaultValue("-1") int numQueries) {
        ServerErrorPayload error = null;
        InputStream samples = RetrieveAndRankProxyResource.class.getResourceAsStream("/SampleQueries.json");
        if (samples != null) {
            try(InputStreamReader streamReader = new InputStreamReader(samples)){
                LineReader reader = new LineReader(streamReader);
                String line = reader.readLine();
                String json = null;
                while (line != null) {
                    if(json != null){
                        json += "\n" + line;
                    }
                    else{
                        json = line;
                    }
                    line = reader.readLine();
                }
                JsonObject contents = new JsonParser().parse(json).getAsJsonObject();
                JsonArray queries = contents.get("queries").getAsJsonArray();
                int max = queries.size() - 1;
                if(numQueries == -1){
                    numQueries = max;
                }
                else{
                    numQueries = Math.min(numQueries, max);
                }
                Random randomizer = new Random();
                SampleQueriesPayload payload = new SampleQueriesPayload();
                for(int i = 0; i < numQueries; i++){
                    int index = randomizer.nextInt(max - 1);
                    JsonObject query = queries.get(index).getAsJsonObject();
                    SampleQueryPayload sqp = new SampleQueryPayload();
                    sqp.setId(i);
                    sqp.setQueryId(query.get("id").getAsInt());
                    sqp.setQuery(query.get("query").getAsString());
                    payload.addQuery(sqp);
                }
                return Response.ok(payload).type(MediaType.APPLICATION_JSON).build();
            } catch (IOException e) {
                String message = Messages.getString("RetrieveAndRankProxyResource.RNR_SAMPLE_QUERIES_ERROR");//$NON-NLS-1$
                error = new ServerErrorPayload(message);
                UtilityFunctions.logger.error(message, e); 
            }
        }
        return Response.serverError().entity(error).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Performs a query against the solr retrieve service and then makes a call to rank the results.
     * The order of the results after both calls is recorded and the returned results are noted in each payload.
     * Once the ranked results are retrieved a third API call is made to the solr retrieve service to retrieve the
     * body (text) for each result. A final lookup is performed to get the ground truth relevance value for each
     * returned result. This final lookup would not normally be performed, but as a goal of this app is to show the 
     * user how training affects the final results of the ranker, we return that info also.
     * 
     * @param body  a query object which contains a textual query, and potentially a query ID to allow us to perform
     * ground truth lookup. The incoming payload is described by: {@link IncomingQueryPayload}
     * 
     * @return
     */
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response postQueryToSolrAndRanker(IncomingQueryPayload body) {
        RetrieveAndRankPayload payload = new RetrieveAndRankPayload(); //payload which will eventually be returned to client
        payload.setQuery(body.getQuery());
        try (ByteArrayOutputStream solrResultsToRank = new ByteArrayOutputStream(); ByteArrayOutputStream solrResultsNoRank = new ByteArrayOutputStream()){
            int resultSize = solrRuntimeQuery(body.getQuery(), true, solrResultsToRank);
            payload.setNum_solr_results(resultSize);
            //The following call is made to just have a reference set of results.
            //When we return the results to the client
            solrRuntimeQuery(body.getQuery(), false, solrResultsNoRank); 
            // upload the runtime query w/ feature vectors to the ranker
            final URI rankURL = new URI(rankerURL + "/rank");
            try (CloseableHttpClient httpClient = UtilityFunctions.createHTTPClient(rankURL, username, password)) {
                final HttpPost rankPost = new HttpPost(rankURL);
                final MultipartEntityBuilder meb = MultipartEntityBuilder.create();
                byte[] arr = solrResultsToRank.toByteArray();
                meb.addBinaryBody("answer_data", IOUtils.toInputStream(new String(arr)));
                rankPost.setEntity(meb.build());
                List<String> finalRank = new ArrayList<>();
                List<String> solrRank = new ArrayList<>();
                try (CloseableHttpResponse response = httpClient.execute(rankPost)) {
                    final String entityResponse = EntityUtils.toString(response.getEntity());
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        String message = MessageFormat.format(Messages.getString("RetrieveAndRankProxyResource.RNR_FAILED_TO_RANK"), response.getStatusLine().getStatusCode());
                        UtilityFunctions.logger.error(message);
                        return Response.serverError().entity(new ServerErrorPayload(message)).build();
                    }
                    //Parse the response from the rank service.
                    JsonObject jo = new JsonParser().parse(entityResponse).getAsJsonObject();
                    JsonArray answers = jo.get("answers").getAsJsonArray();
                    //We only want to fix at most 3 results
                    int len = Math.min(answers.size(), 3);
                    ArrayList<Answer> answerList = new ArrayList<>();
                    for (int i = 0; i < len; i++) {
                        Answer a = new Answer();
                        JsonObject ans = answers.get(i).getAsJsonObject();
                        a.setAnswerId(ans.get("answer_id").getAsString());
                        a.setScore(ans.get("score").getAsFloat());
                        a.setConfidence(ans.get("confidence").getAsDouble());
                        a.setFinalRank(i); //Set the position of the result in the list of ranked results (we add 1 so we don't end up with 0 in the ui)
                        if(body.getQueryId() != -1 && groundTruth != null){
                            //For this app we want to show how the ground truth affects the position of the ranked results
                            //Each of the canned queries has an ID and associated answers
                            if(groundTruth.has(String.valueOf(body.getQueryId()))){
                                //It is a canned query, now we check if the answer is in the GT
                                JsonObject gtForQuery = groundTruth.get(String.valueOf(body.getQueryId())).getAsJsonObject();
                                if(gtForQuery.has(a.getAnswerId())){
                                    //Get the Ground Truth relevance from the ground truth file
                                    a.setRelevance(gtForQuery.get(a.getAnswerId()).getAsInt());
                                }
                                else if(body.getQueryId() != -1){
                                    a.setRelevance(0);
                                }
                            }
                        }
                        answerList.add(a);
                    }
                    for (int i = 0; i < answers.size() - 1; i++) {
                        //We want to set the final rank of all answers from the solr retrieve
                        JsonObject ans = answers.get(i).getAsJsonObject();
                        String id = ans.get("answer_id").getAsString();
                        finalRank.add(id);
                    }
                    payload.setRanked_results(answerList);
                }
                arr = solrResultsNoRank.toByteArray();
                try (CSVReader reader = new CSVReader(new InputStreamReader(IOUtils.toInputStream(new String(arr))), CSV_DELIM, CSVWriter.NO_QUOTE_CHARACTER)) {
                    String[] columns = reader.readNext(); //read header
                    columns = reader.readNext(); //read first row
                    ArrayList<Answer> answerList = new ArrayList<>();
                    int i = 0;
                    while (columns != null && columns.length > 1) {
                        String id = columns[0];
                        solrRank.add(id);
                        if(i++ < 3){
                            Answer a = new Answer();
                            a.setAnswerId(id);
                            a.setScore(Float.parseFloat(columns[1]));
                            a.setSolrRank(i);
                            if(body.getQueryId() != -1 && groundTruth != null){
                                //If it is a canned query, get ground truth info
                                if(groundTruth.has(String.valueOf(body.getQueryId()))){
                                    JsonObject gtForQuery = groundTruth.get(String.valueOf(body.getQueryId())).getAsJsonObject();
                                    if(gtForQuery.has(a.getAnswerId())){
                                        a.setRelevance(gtForQuery.get(a.getAnswerId()).getAsInt());
                                    }
                                    else if(body.getQueryId() != -1){
                                        a.setRelevance(0);
                                    }
                                }
                            }
                            answerList.add(a);
                        }
                        columns = reader.readNext();
                    }
                    payload.setSolr_results(answerList);
                }
                ArrayList<String> idsOfDocsToRetrieve = new ArrayList<>();
                //We only deal with the first three solr and rank results above.. 
                //We need to go through all results and add the solr/rank position
                //This allows us to show the position of the result in the opposing search..
                //For instance we can say result X in the ranked results was at position k in the solr results
                for (Answer answer : payload.getRanked_results()) {
                    idsOfDocsToRetrieve.add(answer.getAnswerId());
                    answer.setSolrRank(solrRank.indexOf(answer.getAnswerId()));//add 1 as we don't want -
                }

                for (Answer answer : payload.getSolr_results()) {
                    idsOfDocsToRetrieve.add(answer.getAnswerId());
                    answer.setFinalRank(finalRank.indexOf(answer.getAnswerId())); //add 1 so we don't end up with zero index in ui
                }

                final ModifiableSolrParams params = new ModifiableSolrParams();
                final SolrDocumentList docs = solrClient.getById(COLLECTION_NAME, idsOfDocsToRetrieve, params);
                final Iterator<SolrDocument> it = docs.iterator();
                HashMap<String, SolrResult> idsToDocs = new HashMap<>();
                while (it.hasNext()) {
                    //get the full search results
                    final SolrDocument doc = it.next();
                    SolrResult result = new SolrResult();
                    result.body = doc.getFirstValue("body").toString().trim();
                    result.id = doc.getFirstValue("id").toString();
                    result.title = doc.getFirstValue("title").toString().trim();
                    idsToDocs.put(result.id, result);
                }
                //Update the solr and rank results with full info
                for (Answer answer : payload.getRanked_results()) {
                    answer.setBody(idsToDocs.get(answer.getAnswerId()).body);
                    answer.setTitle(idsToDocs.get(answer.getAnswerId()).title);
                }

                for (Answer answer : payload.getSolr_results()) {
                    answer.setBody(idsToDocs.get(answer.getAnswerId()).body);
                    answer.setTitle(idsToDocs.get(answer.getAnswerId()).title);
                }
            }
        } catch (IOException e) {
            String message =  Messages.getString("RetrieveAndRankProxyResource.RNR_IO_EXCEPTION_IN_QUERY");
            ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        } catch (SolrServerException e) {
            String message =  Messages.getString("RetrieveAndRankProxyResource.RNR_SOLR_EXCEPTION_IN_QUERY");
            ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        } catch (InterruptedException e) {
            String message =  Messages.getString("RetrieveAndRankProxyResource.RNR_PROCESS_QUERY_IN_QUERY");
            ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        } catch (URISyntaxException e) {
            String message =  Messages.getString("RetrieveAndRankProxyResource.RNR_URI_EXCEPTION_IN_QUERY");
            ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        }
        return Response.ok(payload).build();
    }

    private static int solrRuntimeQuery(String query, boolean featureVector, OutputStream os) throws IOException, SolrServerException, InterruptedException {
        try (Writer w = new OutputStreamWriter(os); final CSVWriter csvWriter = new CSVWriter(w, CSV_DELIM, CSVWriter.NO_QUOTE_CHARACTER)) {
            boolean headersPrinted = false;
            final SolrQuery featureSolrQuery = new SolrQuery(query);
            // specify the fcselect request handler for the feature query
            if (featureVector) {
                featureSolrQuery.setRequestHandler(FCSELECT_REQUEST_HANDLER);
            }

            // bring back the id, score, and featureVector for the feature query
            featureSolrQuery.setParam(FIELD_LIST_PARAM, ID_FIELD, SCORE_FIELD, FEATURE_VECTOR_FIELD);
            // need to ask for enough rows to ensure the correct answer is included in the resultset
            featureSolrQuery.setRows(1000);
            final QueryRequest featureRequest = new QueryRequest(featureSolrQuery);

            // this leverages the plugin
            final QueryResponse featureResponse = processSolrRequest(featureRequest);

            int size = featureResponse.getResults().size();

            final Iterator<SolrDocument> it = featureResponse.getResults().iterator();
            while (it.hasNext()) {
                final SolrDocument doc = it.next();
                final List<String> csvRowValues = new ArrayList<>();

                final Integer answerId = Integer.parseInt((String) doc.getFieldValue(ID_FIELD));
                // if we have GT it's a question-to-feature csv
                csvRowValues.add(String.valueOf(answerId));
                csvRowValues.add(String.valueOf(doc.getFieldValue(SCORE_FIELD)));
                StringTokenizer features;
                int numFeatures = 0;
                if (featureVector) {
                    features = new StringTokenizer(((String) doc.getFieldValue(FEATURE_VECTOR_FIELD)).trim(), FEATURE_VECTOR_DELIM);
                    numFeatures = features.countTokens();
                    while (features.hasMoreTokens()) {
                        final String feature = features.nextToken();
                        csvRowValues.add(feature);
                    }
                }
                // add all the features returned in the vector
                if (!headersPrinted) {
                    headersPrinted = true;
                    final List<String> headerList = new ArrayList<>();
                    headerList.add(ANSWER_ID_HEADER);
                    headerList.add(SCORE_HEADER);
                    for (int i = 1; i <= numFeatures; i++) {
                        headerList.add("f" + i);
                    }
                    csvWriter.writeNext(headerList.toArray(new String[0]));
                }
                // need to add in the headers if this is our first time through
                csvWriter.writeNext(csvRowValues.toArray(new String[0]));
            }
            return size;
        }
    }

    private static QueryResponse processSolrRequest(final QueryRequest request) throws IOException, SolrServerException, InterruptedException {
        int currentAttempt = 0;
        QueryResponse response;
        while (true) {
            try {
                currentAttempt++;
                response = request.process(solrClient, COLLECTION_NAME);
                break;
            } catch (final Exception e) {
                if (currentAttempt < 3) {
                    UtilityFunctions.logger.error("Failed to process Solr service request", e);
                    UtilityFunctions.logger.warn(MessageFormat.format("Retrying Solr request. Attempt {0}", currentAttempt));
                    Thread.sleep(1000);
                } else {
                    throw e;
                }
            }
        }
        return response;
    }

    private static class SolrResult {
        String body;
        String title;
        String id;
    }

}