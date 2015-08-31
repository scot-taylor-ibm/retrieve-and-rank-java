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

    angular.module('rnr.bodyText', [])

    /**
     * @name movie
     * @module modules/movie
     *
     * @description
     *
     * Renders a movie within the UI. The WDS API will notify the movieapp
     * server side code to get a list of movies from themoviedb.org. At this point
     * a list of movies will be returned and a <movie> element added for each movie.
     * The movie is a clickable element which causes the preview panel to load.
     *
     * @param {object}
     *            content - a reference to the movie object the element represents.
     */
    .directive('bodyText', function ($parse, $timeout, $compile) {
        return {
            'restrict': 'E',
            'link': function (scope, element, attr) {
                var content = attr.content;
                var html = [];
                var newElement = null;
                if (content) {
                    if (content.length > 250) {
                        html.push('<span class=\"rnr-truncated\">' + content.substring(0, 200) + ' <a class=\"rnr-showMoreShowLessLink\" ng-click=\"rnrController.showMore($event)\">[show more...]</a></span>');
                        html.push('<span class=\"rnr-not-truncated\">' + content + ' <a class=\"rnr-showMoreShowLessLink\" ng-click=\"rnrController.showLess($event)\">[show less...]</a></span>');
                    }
                    else {
                        html.push('<span>' + content + '</span>');
                    }
                    newElement = $compile(html.join(''))(scope);
                    element.append(newElement);
                }
            }
        };
    });
}());
