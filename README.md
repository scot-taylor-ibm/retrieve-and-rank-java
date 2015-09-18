# Retrieve and Rank Demo app
The Retrieve and Rank demo application showcases the best practices in building search application which makes use of Natural Language Processing. Here's a [quick demo](http://watson-rnr-demo.mybluemix.net/rnr-demo/dist/index.html#/).

The application uses the Watson Retrieve and Rank service to find 'hits' (results) in a corpus of data for a specific query (from the user) and then reorder those results based on machine learning algorithms.

## How it works
The application is configured to use the "Cranfield data set" which is a public domain data set.. Further text here to explain the data, and how the ranker is trained..

[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/watson-developer-cloud/retrieve-and-rank-java)

## Before you begin
Ensure that you have the following prerequisites before you start:
* An IBM Bluemix account. If you don't have one, sign up for it [here](https://apps.admin.ibmcloud.com/manage/trial/bluemix.html?cm_mmc=WatsonDeveloperCloud-_-LandingSiteGetStarted-_-x-_-CreateAnAccountOnBluemixCLI). For more information about the process, see [Developing Watson applications with Bluemix](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/getting_started/gs-bluemix.shtml).
* [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7 or later releases
* [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/marsr)
* [Apache Maven](https://maven.apache.org/download.cgi) 3.1 or later releases
* [Git](https://git-scm.com/downloads)
* [Websphere Liberty Profile server](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-non-eclipse-environments/), if you want to run the app in your local environment.

## Setup
In order to run the Retrieve and Rank demo app, you need to have a configure an instance of the Retrieve and Rank service. The following steps will guide you through the process. The instructions use Eclipse, but you can use the IDE of your choice.

### Get the project from GitHub
1. Clone the retrieve-and-rank-java repository from GitHub by issuing one of the following commands in your terminal:
   ```
   git clone https://github.com/watson-developer-cloud/retrieve-and-rank-java.git
   ```
   ```
   git clone git@github.com:watson-developer-cloud/retrieve-and-rank-java.git
   ```

2. Add the newly cloned repository to your local Eclipse workspace.

### Set up the Bluemix Environment
#### Creating an App
1. [Log in to Bluemix](https://console.ng.bluemix.net/) and navigate to the *Dashboard* on the top panel.
2. Create your app.
      1. Click **CREATE AN APP**.
      2. Select **WEB**.
      3. Select the starter **Liberty for Java**, and click **CONTINUE**.
      4. Type a unique name for your app, such as `rnr-sample-app`, and click **Finish**.
      5. Select **CF Command Line Interface**. If you do not already have it, click **Download CF Command Line Interface**. This link opens a GitHub repository. Download and install it locally.

#### Adding an instance of the Retrieve and Rank service to your App
Complete one of the following sets of steps to add an instance of the Retrieve and Rank service. Bluemix allows you to create a new service instance to bind to your app or to bind to an existing instance. Choose one of the following ways:  

**Creating a new service instance to bind to your app**
  1. [Log in to Bluemix](https://console.ng.bluemix.net/) and navigate to the *Dashboard* on the top panel. Find the app that you created in the previous section, and click it.
  2. Click **ADD A SERVICE OR API**.
  3. Select the **Watson** category, and select the **Retrieve and Rank** service (note, initially at least the Retrieve and Rank service is housed in the 'Labs' section which requires you to click on the "Bluemix Labs Catalog" link at the bottom of the service selection page).
  4. Ensure that your app is specified in the **App** dropdown on the right-hand side of the pop-up window under **Add Service**.
  5. Type a unique name for your service in the **Service name** field, such as `rnr-sample-service`.
  6. Click **CREATE**. The **Restage Application** window is displayed.
  7. Click **RESTAGE** to restart your app. If the app is not started, click **START**.

**Binding to an existing service instance**
  1. [Log in to Bluemix](https://console.ng.bluemix.net/) and navigate to the *Dashboard* on the top panel. Locate and click on the app you created in the previous section.
  2. Click **BIND A SERVICE OR API**.
  3. Select the existing Retrieve and Rank service that you want to bind to your app, and click **ADD**. The **Restage Application** window is displayed.
  4. Click **RESTAGE** to restart your app.  

#### Training the Service
Now that we have a WDS instance bound to the app, we can use the credentials we received in the previous step to configure and train the service. The following document, <a href="https://watson.mybluemix.net/branch/docs-retrieve-rank-ga/doc/retrieve-rank/get_start.shtml" target="_blank">Getting started with the Retrieve and Rank service</a>, explains how to configure the service, create a document collection, upload a corpus of data, and create and train a ranker. Please make sure to carefully follow the steps in the document, taking note of the following artifacts:   
1. Cluster ID   
2. Collection name   
3. Ranker ID   


#### Setting up environment variables in Bluemix
  In order to run the Retrieve and Rank demo application on Bluemix three environment variables are required:  
  1. **CLUSTER_ID**: This is the Apache Solr Cluster ID that is generated by the service. The cluster ID should have been noted while configuring the service in **Stage 2** of the _**Training the Service**_ section above.    
  2. **COLLECTION_NAME**: This is the Apache Solr collection name that is provided to the service. The collection name should have been noted while configuring the service in **Stage 3** of the _**Training the Service**_ section above.   
  3. **RANKER_ID**: This is the Ranker ID that is returned by the service. The Ranker ID should have been noted while configuring the service in **Stage 4** of the _**Training the Service**_ section above.   

Navigate to the application dashboard in Bluemix. Locate and click on the application you created previously. Navigate to the _**Environment Variables**_ section of the UI. Switch to the _**USER-DEFINED**_ tab within the UI. Add three new environment variables as specified above, **CLUSTER_ID** as the key for one, with its value being the cluster ID assigned to the Solr Cluster. **COLLECTION_NAME** as the key for the second variable, its value being that of the Solr Document Collection and the final key being **RANKER_ID**, its value being the system generated ID of the trained ranker.  

To view the home page of the app, open [https://yourAppName.mybluemix.net](https://yourAppName.mybluemix.net), where yourAppName is the name of your app.

### Build the app
This project is configured to be built with Maven. To deploy the app, complete the following steps in order:
  1. In your Eclipse window, expand the *retrieve-and-rank-java* project that you cloned from GitHub.
  2. Right-click the project and select `Maven -> Update Project` from the context menu to update Maven dependencies.
  3. Keep the default options, and click **OK**.
  4. Navigate to the location of your default deployment server. For Websphere Liberty, it would be something like *../LibertyRuntime/usr/servers/<server-name>*. Open the `server.env` file(create one if it doesn't exist), and update the following entries:
    * **VCAP_SERVICES**. This entry should contain a JSON object obtained from the *Environment Variables* section of your application on Bluemix. When entering the JSON in the server.env file make sure it is formatted to be in one line.
    * **CLUSTER_ID**. Specify the ID value that corresponds to your Solr cluster created as a part of the _**Training the Service**_ section above(the cluster id is a long alpha-numeric string).   
    * **COLLECTION_NAME**. Specify the name value that corresponds to your Solr document collection as a part of the _**Training the Service**_ section above.   
    * **RANKER_ID**. Specify the ID value that corresponds to your Ranker created as a part of the _**Training the Service**_ section above(the ranker id is a long alpha-numeric string).   
    Finally, the server.env should look something like this:
   

    ```
    VCAP_SERVICES={"retrieve_and_rank": [{"name": "Watson Retrieve and Rank","label": "retrieve_and_rank","plan": "standard","credentials": {"url": "https://gateway-s.watsonplatform.net/retrieveandrank/api", "username": "system_generated_username", "password": "system_generated_password" } } ] }
CLUSTER_ID=system_generated_cluster_id
COLLECTION_NAME=user_provided_collection_name
RANKER_ID=system_generated_ranker_id
    ```
  5. Switch to the navigator view in Eclipse, right-click the `pom.xml`, and select `Run As -> Maven Install`. Installation of Maven begins. During the installation, the following tasks are done:
    * The JS code is compiled. That is, the various Angular JS files are aggregated, uglified, and compressed. Various other pre-processing is performed on the web code, and the output is copied to the `retrieve-and-rank-java/src/main/webapp/dist` folder in the project.
    * The Java code is compiled, and JUnit tests are executed against the Java code. The compiled Java and JavaScript code and various other artifacts that are required by the web project are copied to a temporary location, and a `.war` file is created.   

This WAR file that resides in */retrieve-and-rank-java/target directory* will be used to deploy the application on Bluemix in the next section.


### Deploy the app
You can run the application on a local server or on Bluemix. Choose one of the following methods, and complete the steps:
#### Deploying the app on your local server in Eclipse
1. Start Eclipse, and click `Window -> Show View -> Servers`.
2. In the **Servers** view, right-click and select `New -> Server`. The *Define a New Server* window is displayed.
3. Select the **WebSphere Application Server Liberty Profile**, and click **Next**.  
4. Configure the server with the default settings.  
5. In the **Available** list in the **Add and Remove** dialog, select the *retrieve-and-rank-java* project, and click **Add >**. The project is added to the runtime configuration for the server in the **Configured** list.
6. Click **Finish**.
7. Copy the *server.env* file which was edited previously from *retrieve-and-rank-java/src/it/resources/server.env* to the root folder of the newly defined server (i.e. *wlp/usr/defaultserver/server.env*).  
8. Start the new server, and open [http://localhost:serverPort/rnr-demo/dist/index.html#/](http://localhost:serverPort/rnr-demo/dist/index.html#/) in your favorite browser, where yourAppName is the specific name of your app.
9. Execute the queries against the service!

#### Deploying the app on the Websphere Liberty Profile in Bluemix
Deploy the WAR file that you built in the previous section by using Cloud Foundry commands.
1. Open the command prompt.
2. Navigate to the directory that contains the WAR file you that you generated by running the following command in the terminal:
   ```
   cd retrieve-and-rank-java/target
   ```

3. Connect to Bluemix by running the following command:
   ```
   cf api https://api.ng.bluemix.net
   ```

4. Log in to Bluemix by running the following command,
   ```
   cf login -u <yourUsername> -o <yourOrg> -s <yourSpace>
   ```
where *yourUsername* is your Bluemix id, *yourOrg* is your organization name in Bluemix and *yourSpace* is your space name in Bluemix.
5. Deploy the app to Bluemix by running the following command.
   ```
   cf push <yourAppName> -p rnr-demo.war
   ```
where, *yourAppName* is the name of your app.
6. Navigate to [Bluemix](https://console.ng.bluemix.net/) to make sure the app is started. If not, click START.
7. To view the home page of the app, open [https://yourAppName.mybluemix.net/rnr-demo/dist/index.html#/](https://yourAppName.mybluemix.net/rnr-demo/dist/index.html#/), where yourAppName is the specific name of your app.
8. Execute the queries against the service!



## Reference information
* [Retrieve and Rank service documentation](https://watson.mybluemix.net/branch/docs-retrieve-rank-ga/doc/retrieve-rank/): Get an in-depth knowledge of the Retrieve and Rank service
* [Configuring the Retrieve and Rank service](https://watson.mybluemix.net/branch/docs-retrieve-rank-ga/doc/retrieve-rank/configure.shtml): Understand how to configure the Retrieve and Rank service
* [Retrieve and Rank API Explorer](https://watson.mybluemix.net/branch/docs-retrieve-rank-ga/apis/#!/retrieve-rank): Explore the various APIs of the Retrieve and Rank service.