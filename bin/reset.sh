#!/bin/sh
# Shell Script to reset device configuration for BoT-Java-SDK

## Logfile path
logFile="/tmp/java-sdk-reset.log"
rm -rf $logFile

## Java SDK log file
sdkLog="/tmp/java-sdk.log.*"
rm -rf $sdkLog

echo "--------------------------------------------------------" | tee -a $logFile
echo "|     Reset Device Configurationfor BoT-Java-SDK       |" | tee -a $logFile
echo "--------------------------------------------------------" | tee -a $logFile

## Check Java is installed and available
javaPath=`which java`
if [ -z "$javaPath" ] ; then
  echo "Java is not installed, Run 'make install' and try again !!!" | tee -a $logFile
  exit 1
fi
echo "Java is installed at $javaPath" | tee -a $logFile

## Check Redis is already installed, it not exit
redisPath=`which redis-server`
if [ -z "$redisPath" ] ; then
  echo "Redis is not installed, Run 'make install' and try again !!!" | tee -a $logFile
  exit 1
fi
echo "Redis Server installed at $redisPath"

## Check redis-server is up and running, if not start it
redisRunning=`ps -aef | grep '^redis'`
if [ -z "$redisRunning" ] ; then
  echo "Starting Redis Server as background process..." | tee -a $logFile
  if [ -n "$redisPath" ] ; then
    $redisPath | tee -a $logFile &
    redisRunning=`ps -aef | grep '^redis'`
  else
    echo "Redis Server not available, Run 'make install' and try again !!!" | tee -a $logFile
    exit 1
  fi
fi
echo $redisRunning | tee -a $logFile

## Get required reset details
echo "Reset DeviceId [Y/N] : \c" ; read resetId
if [ $resetId = 'Y' -o $resetId = 'y' ] ; then
  resetId="true"
else
  resetId="false"
fi
echo "Reset DeviceId: $resetId" | tee -a $logFile

echo "Reset Device Name [Y/N] : \c" ; read resetName
if [ $resetName = 'Y' -o $resetName = 'y' ] ; then
  resetName="true"
else
  resetName="false"
fi
echo "Reset Device Name: $resetName" | tee -a $logFile

## Invoke BoT-Java-SDK reset option to reset the device
sdkJarPath="target/BoT-Java-SDK.jar"
if [ -f $sdkJarPath ] ; then
  echo "BoT-Java-SDK Jar Path: $sdkJarPath" | tee -a $logFile
  java -Dreset.id=$resetId -Dreset.name=$resetName \
       -Djava.util.logging.config.file=target/logging.properties -jar $sdkJarPath reset
  if [ $? -eq 0 ] ; then
    echo "Device reset Completed, refer to $sdkLog for details" | tee -a $logFile
  else
    echo "Device reset Failed, refer to $sdkLog for details" | tee -a $logFile
    exit 1
  fi
else
  echo "BoT-Java-SDK Jar Path: $sdkJarPath Invalid !!!"
  exit 1
fi

echo "----------------------------------------" | tee -a $logFile
echo "|                 DONE                 |" | tee -a $logFile
echo "----------------------------------------" | tee -a $logFile
