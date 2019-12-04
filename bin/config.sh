#!/bin/sh
# Shell Script to cofigure the device settings for Java SDK

echo "---------------------------------------------------" | tee -a $logFile
echo "|     Configuring Device to use BoT-Java-SDK      |" | tee -a $logFile
echo "---------------------------------------------------" | tee -a $logFile

##Get reference to process working directory
processDirectory=`pwd`
echo "Present Working Direcory: $processDirectory" | tee -a $logFile

## Configure log file
logFile="/tmp/java-sdk-config.log"
rm -rf $logFile

## Java SDK log file
sdkLog="/tmp/java-sdk.log.*"
rm -rf $sdkLog

## Check for Redis Server
## If not installed yet, install Redis Server from default Raspbian repository
redisPath=`which redis-server`
if [ -z "$redisPath" ] ; then
  echo "Installing Redis Server from the repository" | tee -a $logFile
  sudo apt-get install redis | tee -a $logFile
  redisPath=`which redis-server`
fi

## Make sure redis-server is up and running
redisRunning=`ps -aef | grep '^redis'`
## Start the Redis server, if not already running
if [ -z "$redisRunning" ] ; then
  echo "Starting Redis Server as background process..." | tee -a $logFile
  if [ -n "$redisPath" ] ; then
    $redisPath | tee -a $logFile &
    redisRunning=`ps -aef | grep '^redis'`
  else
    echo "Redis Server not available, check the log $logFile for details" | tee -a $logFile
    exit 1
  fi
fi
echo $redisRunning | tee -a $logFile

## Collect required configuration details from the user
echo "Provide below required configuration details: " | tee -a $logFile
echo "Maker ID, get from maker portal https://maker.bankingofthings.io/account : \c" ; read makerId
echo "Maker ID: $makerId" | tee -a $logFile

if [ -z "$makerId" ] ; then
  echo "Maker ID cannot be null, exiting..." | tee -a $logFile
  exit 1
fi

echo "Device Name to label the device on FINN APP and maker portal: \c" ; read deviceName
echo "DeviceName: $deviceName" | tee -a $logFile

echo "Generate DeviceId - UUID4 string to uniquely identify the device for payments [Y/N] : \c" ; read generateId
if [ $generateId = 'Y' -o $generateId = 'y' ] ; then
  generateId="true"
else
  generateId="false"
fi
echo "Generate DeviceId: $generateId" | tee -a $logFile

echo "Multipair Device - Allows the device to have multiple alternative names [Y/N] : \c" ; read multiPair
if [ $multiPair = 'Y' -o $multiPair = 'y' ] ; then
  multiPair="true"
  echo "Alternative Device ID - Alternative name for multipair device : \c" ; read altId
  if [ -z "$altId" ] ; then
    echo "Multipair Device, Alternative ID cannot be null, Exiting..." | tee -a $logFile
    exit 1
  fi
else
  multiPair="false"
fi
echo "Multipair Device: $multiPair" | tee -a $logFile
echo "Device Alternative Id: $altId" | tee -a $logFile

## Invoke BoT-Java-SDK config option to configure and activate the device
sdkJarPath="target/BoT-Java-SDK.jar"
if [ -f $sdkJarPath ] ; then
  echo "BoT-Java-SDK Jar Path: $sdkJarPath" | tee -a $logFile
  java -Dmaker.id=$makerId -Ddevice.name=$deviceName -Dgenerate.id=$generateId \
       -Dmulti.pair=$multiPair -Dalternate.id=$altId -Dbleno.service.path=target \
       -Djava.util.logging.config.file=target/logging.properties -jar $sdkJarPath config
  if [ $? -eq 0 ] ; then
    echo "Device Configuration Completed, refer to $sdkLog for details" | tee -a $logFile
  else
    echo "Device Configuration Failed, refer to $sdkLog for details" | tee -a $logFile
    exit 1
  fi
else
  echo "BoT-Java-SDK Jar Path: $sdkJarPath Invalid !!!"
  exit 1
fi

echo "----------------------------------------" | tee -a $logFile
echo "|                 DONE                 |" | tee -a $logFile
echo "----------------------------------------" | tee -a $logFile
