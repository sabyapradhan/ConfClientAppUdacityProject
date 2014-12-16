Generate the Client Library for the Endpoints API

You need to generate the client library that contains your Endpoints API so you can include it in your mobile app.

For Android Apps:

    In a terminal window, cd to the library containing the pom.xml for your application.
    mvn appengine:endpoints_get_client_lib
    cd to the target directory.
    cd to endpoints-client-libs
    cd conference
    mvn install
    cd to the target folder
    Add the client library jar appname-v1-1.n.n-rc-SNAPSHOT.jar to the project for your mobile application. 

If you change your backend API, you'll need to update the client library.