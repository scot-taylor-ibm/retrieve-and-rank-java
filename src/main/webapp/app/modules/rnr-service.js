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
(function () {
    'use strict';
    /*global escape: true */
    angular.module('rnr.service', [])

    /**
     * @name dialogService
     * @module dialog/service
     * @description
     *
     * Implements the dialogService interface using the Watson Theaters App API to interface with the
     * Watson Dialog Service (WDS) and themoviedb.org's movie API.
     */
    .service('retrieveAndRankService', function (_, $http, $q) {
        var queries = null;

        /**
         * Retrieves an array of sample queries to use against the service
         * @public
         * @return {Array} an array of sample queries
         */
        var getSampleQueries = function () {
            if (queries) {
                $q.then({ 'queries': queries });
            }
            return $http.get('../api/bluemix/sampleQueries', {}).then(function (response) {
                response.data.queries.forEach(function (q) {
                    q.escapedQuery = escape(q.query);
                });
                queries = { 'queries': response.data.queries };
                return queries;
            }, function (errorResponse) {
                var data = errorResponse;
           });
        };
        /**
         * Sends a pre-canned or user defined query to the service (via the server proxy).
         * @public
         * @param  userQuery  {String} the text entered by the user or the text of the pre-canned query
         * @param  queryId  {Integer} an optional integer ID which can later be used for ground truth lookup
         * @return {Object} the 'retrieved' and 'ranked' results
         */
        var query = function (userQuery, queryId) {
            return $http.post('../api/bluemix/query', { 'query': userQuery, 'queryId': queryId }).then(function (response) {
                var rankedResults = response.data.ranked_results;
                var retrievedResults = response.data.solr_results;
                return { 'ranked': rankedResults, 'retrieved': retrievedResults };
            }, function (errorResponse) {
                var data = errorResponse;
           });
        };

        return {
            'getSampleQueries': getSampleQueries,
            'query': query
        };
    });
}());
