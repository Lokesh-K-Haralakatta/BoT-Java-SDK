#!/bin/sh
# Shell Script to start embed webserver for BoT-Java-SDK

## Logfile path
logFile="/tmp/java-sdk-startServer.log"
rm -rf $logFile

echo "------------------------------------------------------" | tee -a $logFile
echo "|     Starting Embed Webserver for BoT-Java-SDK      |" | tee -a $logFile
echo "------------------------------------------------------" | tee -a $logFile

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

## Check BoT-Java-SDK.jar is available
sdkJarPath="`pwd`/target/BoT-Java-SDK.jar"
ipAddr=`hostname -I | cut -d ' ' -f 1`
port="3001"
if [ -f "$sdkJarPath" ] ; then
  ## Start embed Webserver as background process
  webserverLog="/tmp/BoT-Java-SDK-Webserver.log"
  rm -rf $webserverLog
  rm -rf "/tmp/tomcat\*"
  rm -rf "/tmp/java-sdk.log.\*"
  echo "Starting Webserver as background process, refer to $webserverLog for details..." | tee -a $logFile
  java -Dlogging.config=target/logback.xml -Djava.util.logging.config.file=target/logging.properties -jar \
       $sdkJarPath server &
  ## Wait until server gets started
  while [ ! -f $webserverLog ] ; do
    sleep 10
  done
  serverStarted=`cat $webserverLog | grep 'Started SDKMain in' | tr -s ' ' | cut -d ' ' -f 9`
  echo "serverStarted = $serverStarted" | tee -a $logFile
  while [ -z "$serverStarted" -o "$serverStarted" != "Started" ] ; do
    sleep 10
    echo "Waiting for Webserver to start..." | tee -a $logFile
    serverStarted=`cat $webserverLog | grep 'Started SDKMain in' | tr -s ' ' | cut -d ' ' -f 9`
    echo "serverStarted = $serverStarted" | tee -a $logFile
  done
  echo "BoT-Java-SDK Webserver Started, access server at URL http://$ipAddr:$port/" | tee -a $logFile
else
  echo "BoT-Java-SDK Jar not available, Run 'make install' and try again !!!" | tee -a $logFile
  exit 1
fi

echo "----------------------------------------" | tee -a $logFile
echo "|                 DONE                 |" | tee -a $logFile
echo "----------------------------------------" | tee -a $logFile
