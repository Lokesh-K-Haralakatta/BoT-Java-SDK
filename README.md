![readme-header](readme-header.png)

# BoT-Java-SDK
    Finn BoT SDK to interact with Banking of things Service using Java programming language to enable 
    IoT devices with autonomous payments. For more information, visit us at our website https://makethingsfinn.com/
    
## Prerequisites
* **Hardware Devices**
  - [Raspberry Pi](https://projects.raspberrypi.org/en/projects/raspberry-pi-setting-up/2)
  - Systems running Mac OS, Linux and Windows
  
* **Software Tools**
  - [JDK](https://www.raspberrypi.org/blog/oracle-java-on-raspberry-pi/)
  - [Apache Maven](https://maven.apache.org/)
  - [Redis](https://redis.io/)
  - [Node JS](https://nodejs.org/en/)
  - [Git](https://projects.raspberrypi.org/en/projects/getting-started-with-git/4)
 
 ## Getting Started with SDK on Raspberry Pi Zero as Standalone Server
 * Setup [Raspberry Pi Zero](https://www.raspberrypi.org/products/raspberry-pi-zero-w/)
 * Get SDK Source from [BoT-Java-SDK](https://github.com/BankingofThings/BoT-Java-SDK/tree/master) Repository using git clone    / downloading the zip
 * Go to BoT-Java-SDK directory and perform below steps in sequence to setup the sdk
   - Install all required prequisite packages by executing the command `make install`
   - Download and Install [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
   - Configure the device by executing the command `make configure`
   - Pair the device with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app) using BLE Feature
   - Start the embed webserver present in sdk by executing the command `make startServer`
   - We can also pair the device using the `/qrcode` end point exposed by the webserver with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
 * Define the required payment action at [Maker Portal](https://maker.bankingofthings.io/login)
 * Add Service to paired device on [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
 * Retrieve Actions from Maker Portal by accessing the `/actions` endpoint exposed by the webserver
 * To trigger the payment, post an action by accessing the `/actions` endpoint with Json data having actionId as given below
   - curl -d '{"actionID":"DEFINED_ACTION_ID"}' -H "Content-Type: application/json" -X POST http://server-ip:3001/actions
 * Stop the webserver by executing the command `stopServer`
 * SDK also had got the feature to reset the device configuration by executing the command `make reset`
 * For all the above described steps, the default log files location is `/tmp/java-sdk-*.log`
 
 ## Getting Started with SDK as a Java Library
 ### Build the BoT-Java-SDK Jar
 * Clone the project using `git` or Download the BoT-Java-SDK.zip and extract
 * Go to BoT-Java-SDK directory
 * Execute the command `mvn clean package`
 * On successful build completion, find `BoT-Java-SDK.jar` in the path `BoT-Java-SDK/target`
 
 ### Consume the BoT-Java-SDK Jar as Java Library
 * Include `BoT-Java-SDK.jar` in the classpath
 * SDK provides below listed APIs as part of SDKWrapper Class to be used in Java Application
   - Pair and Activate the IoT Device for autonomous payments - `SDKWrapper.pairAndActivateDevice`
   - Trigger an autonomous payment - `SDKWrapper.triggerAction`
   - Reset the device configuration - `SDKWrapper.resetDeviceConfiguration`
 * For complete details on using BoT-Java-SDK as Java Library, refer to built-in examples available in examples package
 
 ### Built-in WebServer and Java Samples Execution
 * We need below given prerequisite setup files to be copied to the directory from where `BoT-Java-SDK.jar` to be executed
   - Copy `logback.xml` from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy `bleno-service.js` from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy `logging.properties` from the path `BoT-Java-SDK/src/main/resources` to the execution directory
 * Make sure `redis-server` is up and running before executing the `BoT-Java-SDK.jar`
 * To bootstrap the webserver and consume the ReST endpoints, execute the command `java -jar BoT-Java-SDK.jar server`
   - The available end points to consume are /qrcode   /actions   /pairing
   - The server log can be found at the location `/tmp/BoT-Java-SDK-Webserver.log`
 * To run the library sample for single pair, 
   - Update the required fields in the example `SDKWrapperLibSample.java` 
   - Run the command`java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the library sample with custom path for `bleno-service.js`, run the below command
   - `java -Dbleno.service.path=bleno-directory -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the library sample for Multi pair, 
   - Update the required fields in the example `SDKWrapperLibMultiPairSample.java` 
   - Run the command`java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libMultiPairSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the WebServer Sample to consume end points for single pair,
   - Bootstrap the embed webserver present in Java SDK as explained above
   - Update the IP Address of the Webserver and other required details in the example `SDKWebServerSample.java`
   - Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the WebServer Sample to consume end points for multi pair,
   - Bootstrap the embed webserver present in Java SDK as explained above
   - Update the IP Address of the Webserver and other required details in the example `SDKWebServerMultiPairSample.java`
   - Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverMultiPairSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
