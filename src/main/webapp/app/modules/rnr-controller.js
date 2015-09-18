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

    /**
     */
    var RetrieveAndRankController = function (_, $rootScope, $scope, $location, $anchorScroll, $timeout, retrieveAndRankService) {
        var self = this;
        var result = null;
        var ranked = null;
        var retrieved = null;
        var randomIndex = null;
        self.selectedQuery = {};
        self.sampleQueries = [];
        self.initSampleQueries = function () {
            return retrieveAndRankService.getSampleQueries();
        };
        result = self.initSampleQueries();
        if (result) {
            result.then(function (response) {
                self.sampleQueries = response.queries;
            });
        }
        self.sampleClicked = function () {
            randomIndex = Math.floor(Math.random() * self.sampleQueries.length);
            self.queryTxt = self.sampleQueries[randomIndex].query;
            self.query = self.sampleQueries[randomIndex].query;
            self.queryId = self.sampleQueries[randomIndex].queryId;
            self.userQuery = self.sampleQueries[randomIndex];
            self.submit();
        };

        self.submit = function () {
            var promise = null;
            var top = null;
            if (!self.userQuery) {
                return;
            }
            promise = retrieveAndRankService.query(self.query, self.queryId);
            self.currentQuery = self.userQuery;
            self.retrieved = null;
            self.ranked = null;
            promise.then(function (response) {
                self.ranked = response.ranked;
                self.retrieved = response.retrieved;
                delete self.query;
                delete self.userQuery;
                delete self.queryId;
            });
            top = document.getElementById('inputSection').offsetTop;
            window.scrollTo(0, top);
        };

        self.toggleContent = function (event) {
            var target = null;
            var myself = null;
            var resultsItemContainer = null;
            if (event && event.target) {
                target = event.target;
                myself = $(target);
                resultsItemContainer = myself.closest('.results--item-container');
                myself.toggleClass('results--see-more_SHOW');
                resultsItemContainer.find('.results--more-info').toggle('slow');
            }
        };
    };

    angular.module('rnr.controller', [ 'gettext', 'lodash', 'ngRoute', 'ngSanitize', 'rnr.service' ]).config(
            function ($routeProvider) {
                $routeProvider.when('/', {
                    'templateUrl': 'modules/retrieve_and_rank.html',
                    'controller': 'RetrieveAndRankController',
                    'controllerAs': 'rnrController',
                    'reloadOnSearch': false
                });
            }).controller('RetrieveAndRankController', RetrieveAndRankController);
}());
