![readme-header](readme-header.png)

# BoT-Java-SDK
    Finn BoT SDK to interact with Banking of things Service using Java programming language to enable 
    IoT devices with autonomous payments. For more information, visit us at https://makethingsfinn.com/

## Supported Features
   | Sl. No        | SDK Feature                                | Status      | Remarks |
   | :-----------: |:-------------------------------------------| :-----------| :-------|
   |        1      | Pairing through Bluetooth Low Energy (BLE) | :heavy_check_mark: | Supported in Library Mode |
   |        2      | Pairing through QR Code                    | :heavy_check_mark: | Supported only in Webserver mode through end point /qrcode to get generated QRCode for device to be paired |
   |        3      | Secured HTTP with BoT Service              | :heavy_check_mark: | Supported for all interactions with backend server |
   |        4      | Logging                                    | :heavy_check_mark: | Supported with Java Util Logging for SDK and Springboot logging for Webserver. Default log path is /tmp.|
   
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
   - Install all required prequisite packages by executing the command **`make install`**
   - Download and Install [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
   - Configure the device by executing the command **`make configure`**
   - Pair the device with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app) using BLE Feature
   - Start the embed webserver present in sdk by executing the command **`make startServer`**
   - We can also pair the device using the **`/qrcode`** end point exposed by the webserver with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
 * Define the required payment action at [Maker Portal](https://maker.bankingofthings.io/login)
 * Add Service to paired device on [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
 * Retrieve Actions from Maker Portal by accessing the **`/actions`** endpoint exposed by the webserver
 * To trigger the payment, post an action by accessing the **`/actions`** endpoint with Json data having actionId as given below
   - curl -d '{"actionID":"DEFINED_ACTION_ID"}' -H "Content-Type: application/json" -X POST http://server-ip:3001/actions
 * Stop the webserver by executing the command **`make stopServer`**
 * SDK also had got the feature to reset the device configuration by executing the command **`make reset`**
 * For all the above described steps, the default log files location is **`/tmp/java-sdk-*.log`**
 
 ## Getting Started with SDK as a Java Library
 ### Build the BoT-Java-SDK Jar
 * Clone [BoT-Java-SDK](https://github.com/BankingofThings/BoT-Java-SDK/tree/master) using **`git`** or Download the BoT-Java-SDK.zip and extract
 * Go to BoT-Java-SDK directory
 * Execute the command **`mvn clean package`**
 * On successful build completion, find **`BoT-Java-SDK.jar`** in the path **`BoT-Java-SDK/target`**
 
 ### Consume the BoT-Java-SDK Jar as Java Library
 * Include **`BoT-Java-SDK.jar`** in the classpath
 * SDK provides below listed APIs as part of SDKWrapper Class to be used in Java Application
   - Pair and Activate the IoT Device for autonomous payments - **`SDKWrapper.pairAndActivateDevice`**
   - Trigger an autonomous payment - **`SDKWrapper.triggerAction`**
   - Reset the device configuration - **`SDKWrapper.resetDeviceConfiguration`**
 * For complete details on using BoT-Java-SDK as Java Library, refer to built-in examples available in examples package
 
 ### Built-in WebServer and Java Samples Execution
 * We need below given prerequisite setup files to be copied to the directory from where **`BoT-Java-SDK.jar`** to be executed
   - Copy **`logback.xml`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy **`bleno-service.js`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy **`logging.properties`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
 * Make sure **`redis-server`** is up and running before executing the `BoT-Java-SDK.jar`
 * To bootstrap the webserver and consume the ReST endpoints, execute the command **`java -jar BoT-Java-SDK.jar server`**
   - The available end points to consume are /qrcode   /actions   /pairing
   - The server log can be found at the location **`/tmp/BoT-Java-SDK-Webserver.log`**
 * To run the library sample for single pair, 
   - Update the required fields in the example **`SDKWrapperLibSample.java`**
   - Run the command`java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the library sample with custom path for **`bleno-service.js`**, run the below command
   - `java -Dbleno.service.path=bleno-directory -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the library sample for Multi pair, 
   - Update the required fields in the example **`SDKWrapperLibMultiPairSample.java`**
   - Run the command`java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libMultiPairSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the WebServer Sample to consume end points for single pair,
   - Bootstrap the embed webserver present in Java SDK as explained above
   - Update the IP Address of the Webserver and other required details in the example **`SDKWebServerSample.java`**
   - Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * To run the WebServer Sample to consume end points for multi pair,
   - Bootstrap the embed webserver present in Java SDK as explained above
   - Update the IP Address of the Webserver and other required details in the example **`SDKWebServerMultiPairSample.java`**
   - Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverMultiPairSample`
   - The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files

## Contributing
Any improvement to the FINN SDK are very much welcome! Our software is open-source and we believe your input can help create a lively community and the best version of FINN. We’ve already implemented much of the feedback given to us by community members and will continue to do so. Join them by contributing to the SDK or by contributing to the documentation.

# Community

## Slack
Slack is our main feedback channel for the SDK and documentation. Join our [Slack channel](https://ing-bankingofthings.slack.com/join/shared_invite/enQtNDEyODg3MDE1NDg4LWJhNGFiOTFhZmVlNGQwMTM4ZjQzNmZmZDk5ZGZiNjNlZTVjZjNmYjE0Y2MxZjU5MWQxNmY5MTgzYzAxNmFiNGU) and be part of the FINN community.<br/>

## Meetups
We also organize meetups, e.g. demo or hands-on workshops. Keep an eye on our meetup group for any events coming up soon. Here you will be able to see the FINN software in action and meet the team.<br/>
[Meetup/Amsterdam-ING-Banking-of-Things](meetup.com/Amsterdam-ING-Banking-of-Things/).
 
# About FINN
After winning the ING Innovation Bootcamp in 2017, FINN is now part of the ING Accelerator Program. Our aim is to become the new Internet of Things (IoT) payment standard that enables service-led business models. FINN offers safe, autonomous transactions for smart devices.
We believe our software offers tremendous business opportunities. However, at heart, we are enthusiasts. Every member of our team has a passion for innovation. That’s why we love working on FINN.
[makethingsfinn.com](makethingsfinn.com)
