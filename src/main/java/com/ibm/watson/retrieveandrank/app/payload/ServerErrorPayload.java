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

package com.ibm.watson.retrieveandrank.app.payload;

/**
 * A payload object that is sent to the client when an exception/error occurs.
 * The payload contains a message which may be presented to the client.
 */
public class ServerErrorPayload {
    private String message;
    /**
     * Constructor
     * 
     * @param message  the error message which will be sent to the client
     */
    public ServerErrorPayload(String message) {
        this.message = message;
    }
    /**
     * Returns the error message that will be sent to the client
     * @return
     */
    public String getMessage() {
        return message;
    }
    /**
     * Sets the message that is to be sent to the client
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
