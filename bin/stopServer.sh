#!/bin/sh
# Shell Script to stop embed webserver for BoT-Java-SDK

## Logfile path
logFile="/tmp/java-sdk-stopServer.log"
rm -rf $logFile

echo "------------------------------------------------------" | tee -a $logFile
echo "|     Stoping Embed Webserver for BoT-Java-SDK       |" | tee -a $logFile
echo "------------------------------------------------------" | tee -a $logFile

kill -9 `ps -aef | grep 'BoT-Java-SDK.jar server' | tr -s ' ' | cut -d ' ' -f 2` | tee -a $logFile

echo "----------------------------------------" | tee -a $logFile
echo "|                 DONE                 |" | tee -a $logFile
echo "----------------------------------------" | tee -a $logFile
