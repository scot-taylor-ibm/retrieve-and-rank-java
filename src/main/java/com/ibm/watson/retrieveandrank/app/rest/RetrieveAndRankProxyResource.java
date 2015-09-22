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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.google.common.collect.Lists;
import com.google.common.io.LineReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.retrieveandrank.app.payload.IncomingQueryPayload;
import com.ibm.watson.retrieveandrank.app.payload.RankResultPayload;
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
    private final static String SOLR_CLUSTER_PATH = "/solr_clusters/";
    private static String COLLECTION_NAME = "COLLECTION_NAME";
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
            UtilityFunctions.logger
                    .info(Messages.getString("RetrieveAndRankProxyResource.VCAP_SERVICES_ENV_VAR_FOUND")); //$NON-NLS-1$
            JsonObject services = new JsonParser().parse(envServices).getAsJsonObject();
            UtilityFunctions.logger
                    .info(Messages.getString("RetrieveAndRankProxyResource.VCAP_SERVICES_JSONOBJECT_SUCCESS")); //$NON-NLS-1$
            final JsonArray arr = (JsonArray) services.get("retrieve_and_rank"); //$NON-NLS-1$
            if (arr != null && arr.size() > 0) {
                services = arr.get(0).getAsJsonObject();
                final JsonObject credentials = services.get("credentials").getAsJsonObject(); //$NON-NLS-1$
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
            UtilityFunctions.logger.info(Messages.getString("RetrieveAndRankProxyResource.RNR_RANKER_ID_SUCCESS"));
            UtilityFunctions.logger.info(ranker_id);// $NON-NLS-1$
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.RNR_RANKER_ID_FAIL")); //$NON-NLS-1$
        }
        URI solrUri = null;
        try {
            solrUri = new URI(R_N_R_BASE_URL + SOLR_CLUSTER_PATH + cluster_id);
            solrClient = new WatsonSolrClient(solrUri, username, password);
        } catch (final URISyntaxException e) {
            UtilityFunctions.logger
                    .error(MessageFormat.format(Messages.getString("RetrieveAndRankProxyResource.RNR_SOLR_CLIENT_FAIL"), //$NON-NLS-1$
                            R_N_R_BASE_URL + SOLR_CLUSTER_PATH + cluster_id));
        }

        envServices = System.getenv("COLLECTION_NAME"); //$NON-NLS-1$
        if (envServices != null) {
            COLLECTION_NAME = envServices;
            UtilityFunctions.logger
                    .info(Messages.getString("RetrieveAndRankProxyResource.RNR_COLLECTION_NAME_SUCCESS")); //$NON-NLS-1$
        } else {
            UtilityFunctions.logger.error(Messages.getString("RetrieveAndRankProxyResource.RNR_COLLECTION_NAME_FAIL")); //$NON-NLS-1$
        }

        final InputStream samples = RetrieveAndRankProxyResource.class.getResourceAsStream("/GroundTruth.json");
        groundTruth = new JsonParser().parse(new InputStreamReader(samples)).getAsJsonObject();
        System.out.println(groundTruth.toString());
    }

    /**
     * Gets a list of queries from the resource file on disk. The queries are provided by the Cranfield dataset.. This
     * API randomly selects n queries (by default 8) and returns those to the client.
     *
     * @param numQueries
     *            the number of queries the client wishes to retrieve, by default 8
     * @return a list of n queries. The returned query contains a query string and and query ID for ground truth lookup.
     */
    @Path("/sampleQueries")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSampleQueries(@QueryParam("numQueries") @DefaultValue("-1") int numQueries) {
        ServerErrorPayload error = null;
        final InputStream samples = RetrieveAndRankProxyResource.class.getResourceAsStream("/SampleQueries.json");
        if (samples != null) {
            try (InputStreamReader streamReader = new InputStreamReader(samples)) {
                final LineReader reader = new LineReader(streamReader);
                String line = reader.readLine();
                String json = null;
                while (line != null) {
                    if (json != null) {
                        json += "\n" + line;
                    } else {
                        json = line;
                    }
                    line = reader.readLine();
                }
                final JsonObject contents = new JsonParser().parse(json).getAsJsonObject();
                final JsonArray queries = contents.get("queries").getAsJsonArray();
                final int max = queries.size() - 1;
                if (numQueries == -1) {
                    numQueries = max;
                } else {
                    numQueries = Math.min(numQueries, max);
                }
                final Random randomizer = new Random();
                final SampleQueriesPayload payload = new SampleQueriesPayload();
                for (int i = 0; i < numQueries; i++) {
                    final int index = randomizer.nextInt(max - 1);
                    final JsonObject query = queries.get(index).getAsJsonObject();
                    final SampleQueryPayload sqp = new SampleQueryPayload();
                    sqp.setId(i);
                    sqp.setQueryId(query.get("id").getAsInt());
                    sqp.setQuery(query.get("query").getAsString());
                    payload.addQuery(sqp);
                }
                return Response.ok(payload).type(MediaType.APPLICATION_JSON).build();
            } catch (final IOException e) {
                final String message = Messages.getString("RetrieveAndRankProxyResource.RNR_SAMPLE_QUERIES_ERROR");//$NON-NLS-1$
                error = new ServerErrorPayload(message);
                UtilityFunctions.logger.error(message, e);
            }
        }
        return Response.serverError().entity(error).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Performs a query against the solr retrieve service and then makes a call to rank the results. The order of the
     * results after both calls is recorded and the returned results are noted in each payload. Once the ranked results
     * are retrieved a third API call is made to the solr retrieve service to retrieve the body (text) for each result.
     * A final lookup is performed to get the ground truth relevance value for each returned result. This final lookup
     * would not normally be performed, but as a goal of this app is to show the user how training affects the final
     * results of the ranker, we return that info also.
     *
     * @param body
     *            a query object which contains a textual query, and potentially a query ID to allow us to perform
     *            ground truth lookup. The incoming payload is described by: {@link IncomingQueryPayload}
     *
     * @return
     */
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response postQueryToSolrAndRanker(IncomingQueryPayload body) {
        final RetrieveAndRankPayload payload = new RetrieveAndRankPayload(); // pay load which will eventually be
                                                                             // returned to client
        payload.setQuery(body.getQuery());
        try {
            final QueryResponse featureQueryResponse = solrRuntimeQuery(body.getQuery(), true);
            final int resultSize = featureQueryResponse.getResults().size();

            payload.setNum_solr_results(resultSize);

            // The following call is made to just have a reference set of results.
            // When we return the results to the client
            final List<String> finalRank = new ArrayList<>();

            Iterator<SolrDocument> it = featureQueryResponse.getResults().iterator();
            int i = 0;

            final ArrayList<RankResultPayload> answerList = new ArrayList<>();
            while (it.hasNext()) {
                final SolrDocument doc = it.next();
                final String answerId = (String) doc.getFieldValue(ID_FIELD);
                finalRank.add(answerId);

                final String score = String.valueOf(doc.getFieldValue(SCORE_FIELD));

                if (i++ < 3) {
                    final RankResultPayload a = new RankResultPayload();
                    a.setAnswerId((String) doc.getFieldValue(ID_FIELD));
                    a.setScore(Float.parseFloat(score));
                    a.setFinalRank(i);
                    if (body.getQueryId() != -1 && groundTruth != null) {
                        // If it is a canned query, get ground truth info
                        if (groundTruth.has(String.valueOf(body.getQueryId()))) {
                            final JsonObject gtForQuery =
                                    groundTruth.get(String.valueOf(body.getQueryId())).getAsJsonObject();
                            if (gtForQuery.has(a.getAnswerId())) {
                                a.setRelevance(gtForQuery.get(a.getAnswerId()).getAsInt());
                            } else if (body.getQueryId() != -1) {
                                a.setRelevance(0);
                            }
                        }
                    }
                    answerList.add(a);
                }

            }
            payload.setRanked_results(Lists.newArrayList(answerList));
            answerList.clear();

            final QueryResponse solrQueryResponse = solrRuntimeQuery(body.getQuery(), false);
            final List<String> solrRank = new ArrayList<>();

            it = solrQueryResponse.getResults().iterator();
            i = 0;

            while (it.hasNext()) {
                final SolrDocument doc = it.next();
                final String answerId = (String) doc.getFieldValue(ID_FIELD);
                solrRank.add(answerId);

                final String score = String.valueOf(doc.getFieldValue(SCORE_FIELD));

                if (i++ < 3) {
                    final RankResultPayload a = new RankResultPayload();
                    a.setAnswerId((String) doc.getFieldValue(ID_FIELD));
                    a.setScore(Float.parseFloat(score));
                    a.setSolrRank(i);
                    if (body.getQueryId() != -1 && groundTruth != null) {
                        // If it is a canned query, get ground truth info
                        if (groundTruth.has(String.valueOf(body.getQueryId()))) {
                            final JsonObject gtForQuery =
                                    groundTruth.get(String.valueOf(body.getQueryId())).getAsJsonObject();
                            if (gtForQuery.has(a.getAnswerId())) {
                                a.setRelevance(gtForQuery.get(a.getAnswerId()).getAsInt());
                            } else if (body.getQueryId() != -1) {
                                a.setRelevance(0);
                            }
                        }
                    }
                    answerList.add(a);
                }
            }
            payload.setSolr_results(answerList);

            final ArrayList<String> idsOfDocsToRetrieve = new ArrayList<>();
            // We only deal with the first three solr and rank results above..
            // We need to go through all results and add the solr/rank position
            // This allows us to show the position of the result in the opposing search..
            // For instance we can say result X in the ranked results was at position k in the solr results
            for (final RankResultPayload answer : payload.getRanked_results()) {
                idsOfDocsToRetrieve.add(answer.getAnswerId());
                answer.setSolrRank(solrRank.indexOf(answer.getAnswerId()));// add 1 as we don't want -
            }

            for (final RankResultPayload answer : payload.getSolr_results()) {
                idsOfDocsToRetrieve.add(answer.getAnswerId());
                answer.setFinalRank(finalRank.indexOf(answer.getAnswerId())); // add 1 so we don't end up with zero
                                                                              // index in ui
            }

            final ModifiableSolrParams params = new ModifiableSolrParams();
            final SolrDocumentList docs = solrClient.getById(COLLECTION_NAME, idsOfDocsToRetrieve, params);
            it = docs.iterator();
            final HashMap<String, SolrResult> idsToDocs = new HashMap<>();
            while (it.hasNext()) {
                // get the full search results
                final SolrDocument doc = it.next();
                final SolrResult result = new SolrResult();
                result.body = doc.getFirstValue("body").toString().trim();
                result.id = doc.getFirstValue("id").toString();
                result.title = doc.getFirstValue("title").toString().trim();
                idsToDocs.put(result.id, result);
            }
            // Update the solr and rank results with full info
            for (final RankResultPayload answer : payload.getRanked_results()) {
                answer.setBody(idsToDocs.get(answer.getAnswerId()).body);
                answer.setTitle(idsToDocs.get(answer.getAnswerId()).title);
            }

            for (final RankResultPayload answer : payload.getSolr_results()) {
                answer.setBody(idsToDocs.get(answer.getAnswerId()).body);
                answer.setTitle(idsToDocs.get(answer.getAnswerId()).title);
            }

        } catch (final IOException e) {
            final String message = Messages.getString("RetrieveAndRankProxyResource.RNR_IO_EXCEPTION_IN_QUERY");
            final ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        } catch (final SolrServerException e) {
            final String message = Messages.getString("RetrieveAndRankProxyResource.RNR_SOLR_EXCEPTION_IN_QUERY");
            final ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        } catch (final InterruptedException e) {
            final String message = Messages.getString("RetrieveAndRankProxyResource.RNR_PROCESS_QUERY_IN_QUERY");
            final ServerErrorPayload error = new ServerErrorPayload(message);
            UtilityFunctions.logger.error(message, e);
            return Response.serverError().entity(error).build();
        }
        return Response.ok(payload).build();
    }

    private static QueryResponse solrRuntimeQuery(String query, boolean featureVector)
            throws IOException, SolrServerException, InterruptedException {
        // boolean headersPrinted = false;
        final SolrQuery featureSolrQuery = new SolrQuery(query);
        // specify the fcselect request handler for the feature query

        if (featureVector) {
            featureSolrQuery.setRequestHandler(FCSELECT_REQUEST_HANDLER);
            // add the ranker id - this tells the plugin to re-reank the results in a single pass
            featureSolrQuery.setParam("ranker_id", ranker_id);

        }

        // bring back the id, score, and featureVector for the feature query
        featureSolrQuery.setParam(FIELD_LIST_PARAM, ID_FIELD, SCORE_FIELD, FEATURE_VECTOR_FIELD);

        // need to ask for enough rows to ensure the correct answer is included in the resultset
        featureSolrQuery.setRows(1000);
        final QueryRequest featureRequest = new QueryRequest(featureSolrQuery, METHOD.POST);

        // this leverages the plugin
        final QueryResponse featureResponse = processSolrRequest(featureRequest);

        return featureResponse;
    }

    private static QueryResponse processSolrRequest(final QueryRequest request)
            throws IOException, SolrServerException, InterruptedException {
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
                    UtilityFunctions.logger
                            .warn(MessageFormat.format("Retrying Solr request. Attempt {0}", currentAttempt));
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