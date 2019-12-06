![readme-header](readme-header.png)

# BoT-Java-SDK
    Finn BoT SDK to interact with Banking of things Service using Java programming language to enable 
    IoT devices with autonomous payments. For more information, visit us at https://makethingsfinn.com/

## Supported Features
   | Sl. No        | SDK Feature                                | Status      | Remarks |
   | :-----------: |:-------------------------------------------| :-----------| :-------|
   |        1      | Pairing through Bluetooth Low Energy (BLE) | :thumbsup: | Supported in Library Mode |
   |        2      | Pairing through QR Code                    | :thumbsup: | Supported only in Webserver mode through end point /qrcode to get generated QRCode for device to be paired |
   |        3      | Secured HTTP with BoT Service              | :thumbsup: | Supported for all interactions with backend server |
   |        4      | Logging                                    | :thumbsup: | Supported with Java Util Logging for SDK and Springboot logging for Webserver. Default log path is /tmp.|
   |        5      | Offline Actions                            | :thumbsdown: | Supported with Java Util Logging for SDK and Springboot logging for Webserver. Default log path is /tmp.|
   
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
  
  **Note:** 
  * Make sure the above listed tools are already available for the systems running Mac OS, Linux and Windows. 
  * For Raspberry Pi Zero, the install script `make install` takes care of installing all required software tools.
  * To Disable password for sudo: 
    - Edit the file /etc/sudoers
    - Append line at the end: $USER ALL=(ALL) NOPASSWD: ALL
 
 ## [Maker Portal](https://maker.bankingofthings.io/login)
 * Login to [Maker Portal](https://maker.bankingofthings.io/login) with GitHub Account
 * Define the required payment action at [Maker Portal](https://maker.bankingofthings.io/login) under `Statistics -> Actions -> CREATE SERVICE PAYMENT`
 * Make note of `makerID` and `actionID` to be used in below following sections
   
 ## [FINN Mobile Application](https://docs.bankingofthings.io/mobile-app)
 * Download and Install [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
 * Signup / Signin to FINN Mobile Application using the magic link on mobile phone
 
 ## Getting Started with Java SDK on [Raspberry Pi Zero](https://www.raspberrypi.org/products/raspberry-pi-zero-w/) as Standalone Server
 * Setup [Raspberry Pi Zero](https://www.raspberrypi.org/products/raspberry-pi-zero-w/)
 * Get SDK Source from [BoT-Java-SDK](https://github.com/BankingofThings/BoT-Java-SDK/tree/master) Repository using git clone / downloading the zip
   - Install Git using the command `apt-get install git` if it's not available on Raspbian
 * Go to BoT-Java-SDK directory and perform below steps in sequence to setup the sdk
   - Install all required prequisite packages by executing the command **`make install`**. It may take couple of minutes to complete based on network speed, if it's first time execution
   - Configure the device by executing the command **`make configure`**
   - Pair the device with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app) using BLE Feature
     - Login to FINN Mobile Application and search for BLE Device to pair
   - Start the embed webserver present in sdk by executing the command **`make startServer`**. It may take couple of minutes to successfully start the Webserver.
     - After successful server start, it displays the base url. Make note of IP Address
   - We can also pair the device using the **`/qrcode`** end point exposed by the webserver with the [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
   - Add Service to paired device on [FINN Mobile App](https://docs.bankingofthings.io/mobile-app)
   - Retrieve Actions from Maker Portal by accessing the **`/actions`** endpoint exposed by the webserver
   - To trigger the payment, post an action by accessing the **`/actions`** endpoint with Json data having actionId as given below and replace the `server-ip` with the actual IP Address collected from base url above
       - curl -d '{"actionID":"DEFINED_ACTION_ID"}' -H "Content-Type: application/json" -X POST http://server-ip:3001/actions
   - Stop the webserver by executing the command **`make stopServer`**
   - SDK also had got the feature to reset the device configuration by executing the command **`make reset`** when it's no longer needed
   - For all the above described steps, the default log files location is **`/tmp/java-sdk-*.log`**
   - Go to the [Maker Portal](https://maker.bankingofthings.io/login) under the tab **`Statistics -> Customers -> Click on Customer ID field`** to view the connected devices
   - Go to the [Maker Portal](https://maker.bankingofthings.io/login) to check the autonomous payments status under the tab **`Statistics -> Actions -> Payments -> Click on the Action Name`** to view the action statistics
 
 ## Getting Started with SDK as a Java Library
 ### Build the BoT-Java-SDK Jar
 * Clone [BoT-Java-SDK](https://github.com/BankingofThings/BoT-Java-SDK/tree/master) using **`git`** or Download the BoT-Java-SDK.zip and extract
 * Go to BoT-Java-SDK directory
 * Update the required fields for the samples present in the path `src/main/java/com/finn/bot/examples` like
     - MakerID from [Maker Portal](https://maker.bankingofthings.io/login)
     - actionID from [Maker Portal](https://maker.bankingofthings.io/login)
     - actionTriggerInterval, default is 5 minutes
     - IP Address of the board/system where the Embed Webserver is going to be executed. Update IP Address in the baseURL field present in the webserver samples
 * Execute the command **`mvn clean package`** to build `BoT-Java-SDK.jar` into the path **`BoT-Java-SDK/target`**
 * As quick steps, 
   - Run `make install`, it may take couple of minutes to complete based on network speed, if it's first time execution
   - Run `make startServer`, it may take couple of minutes to successfully start the Webserver. We should see baseURL at the end after successful start of the Webserver.
   - Directly go to section **Java SDK Samples Execution** below
 
 ### Consume the BoT-Java-SDK Jar as Java Library
 * Include **`BoT-Java-SDK.jar`** in the classpath
 * SDK provides below listed APIs as part of SDKWrapper Class to be used in Java Application
   - Pair and Activate the IoT Device for autonomous payments - **`SDKWrapper.pairAndActivateDevice`**
   - Trigger an autonomous payment - **`SDKWrapper.triggerAction`**
   - Reset the device configuration - **`SDKWrapper.resetDeviceConfiguration`**
 * For complete details on using BoT-Java-SDK as Java Library, refer to built-in examples available in examples package
 
 ### Bootstrap embed WebServer for Java SDK 
 * We need below given prerequisite setup files to be copied to the directory from where **`BoT-Java-SDK.jar`** to be executed
   - Copy **`logback.xml`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy **`bleno-service.js`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
   - Copy **`logging.properties`** from the path `BoT-Java-SDK/src/main/resources` to the execution directory
 * Make sure **`redis-server`** is up and running before executing the `BoT-Java-SDK.jar`
 * To bootstrap the webserver and consume the ReST endpoints, execute the command **`java -jar BoT-Java-SDK.jar server`**
   - The available end points to consume are **`/qrcode   /actions   /pairing  /activate`**
   - The server log can be found at the location **`/tmp/BoT-Java-SDK-Webserver.log`**
   - We should see the server access base URL at the end of the successful start of the Java SDK Webserver
   - The Webserver's baseURL can be used by external clients to consume the exposed endpoints
   - Webserver can be stopped by running `make stopServer` from `BoT-Java-SDK/`
   
 ### Java SDK Samples Execution
 #### Single Pair Device Sample
 * Go to `BoT-Java-SDK/target` directory
 * Run the command`java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
 * Pair and Activate the device using [FINN Mobile Application](https://docs.bankingofthings.io/mobile-app) throuhg BLE Service
 * Observe the triggerring of autonomous payments from the device for every configured interval in the application
 * All library samples use **`bleno-service.js`** for pairing through BLE. By default, it should be available in the execution directory, i.e `BoT-Java-SDK/target`
 * To run the library sample with custom path for **`bleno-service.js`**, use the below command
   - `java -Dbleno.service.path=bleno-directory -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
 * Press `<Ctrl-C>` to interrupt and quit the running sample
 * The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) under the tab **`Statistics -> Customers -> Click on Customer ID field`** to view the connected devices
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) to check the autonomous payments status under the tab **`Statistics -> Actions -> Payments -> Click on the Action Name`** to view the action statistics
    
 #### Multi Pair Device Sample
 * Go to `BoT-Java-SDK/target` directory
 * Run the command `java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libMultiPairSample`
 * Pair and Activate the device using [FINN Mobile Application](https://docs.bankingofthings.io/mobile-app) throuhg BLE Service
 * Observe the triggerring of autonomous payments from the device for every configured interval in the application
 * All library samples use **`bleno-service.js`** for pairing through BLE. By default, it should be available in the execution directory, i.e `BoT-Java-SDK/target`
 * To run the library sample with custom path for **`bleno-service.js`**, use the below command
   - `java -Dbleno.service.path=bleno-directory -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample`
 * Press `<Ctrl-C>` to interrupt and quit the running sample
 * The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) to check the autonomous payments status under the tab **`Statistics -> Customers -> Click on Customer ID field`** to view the connected devices
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) under the tab **`Statistics -> Actions -> Payments -> Click on the Action Name`** to view the action statistics
 
 #### WebServer Sample to consume end points for single pair,
 * Bootstrap the embed webserver present in Java SDK by executing `make startServer` from `BoT-Java-SDK/`
 * The server log can be found at the location **`/tmp/BoT-Java-SDK-Webserver.log`**
 * We should see the server access base URL at the end of the successful start of the Java SDK Webserver
 * Go to `BoT-Java-SDK/target` directory
 * Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverSample`
 * Access QrCode for the device from baseURL/qrcode and pair the device using [FINN Mobile Application](https://docs.bankingofthings.io/mobile-app)
 * If pairing can not be done with the stipulated time of max retries, a warning message **WARNING: Device Pairing Failed, check the log for details: /tmp/java-sdk.log.\*** is displayed and the sample quits, rerun the sample using same command as used earlier and try to complete pairing again
 * On successful pairing and activation of the device, we should see the autonomous payments triggered at configured interval of time
 * Press `<Ctrl-C>` to interrupt and quit the running sample
 * The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) under the tab **`Statistics -> Customers -> Click on Customer ID field`** to view the connected devices
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) to check the autonomous payments status under the tab **`Statistics -> Actions -> Payments -> Click on the Action Name`** to view the action statistics
 
 #### WebServer Sample to consume end points for multi pair,
 * Bootstrap the embed webserver present in Java SDK by executing `make startServer` from `BoT-Java-SDK/`
 * The server log can be found at the location **`/tmp/BoT-Java-SDK-Webserver.log`**
 * We should see the server access base URL at the end of the successful start of the Java SDK Webserver
 * Go to `BoT-Java-SDK/target` directory
 * Run the command `java  -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverMultiPairSample`
 * Access QrCode for the device from baseURL/qrcode and pair the device using [FINN Mobile Application](https://docs.bankingofthings.io/mobile-app)
 * If pairing can not be done with the stipulated time of max retries, a warning message **WARNING: Device Pairing Failed, check the log for details: /tmp/java-sdk.log.\*** is displayed and the sample quits, rerun the sample using same command as used earlier and try to complete pairing again
 * On successful pairing and activation of the device, we should see the autonomous payments triggered at configured interval of time
 * Press `<Ctrl-C>` to interrupt and quit the running sample
 * The log entries for Java SDK can be found in `/tmp/java-sdk.log.*` files
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) under the tab **`Statistics -> Customers -> Click on Customer ID field`** to view the connected devices
 * Go to the [Maker Portal](https://maker.bankingofthings.io/login) to check the autonomous payments status under the tab **`Statistics -> Actions -> Payments -> Click on the Action Name`** to view the action statistics
 
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
