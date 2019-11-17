#!/bin/sh
# Shell Script to install all required software tools to build and execute Java SDK Server

## Install Packages log file
logFile="/tmp/java-sdk-install.log"
rm -rf $logFile

echo "-------------------------------------------------" | tee -a $logFile
echo "|     INSTALLING BoT-Java-SDK DEPENDENCIES      |" | tee -a $logFile
echo "-------------------------------------------------" | tee -a $logFile

## Update Debian Packages
#echo "Updating Raspbian Packages from the repository" | tee -a $logFile
#sudo apt-get update | tee -a $logFile

## Check for Java Availability. By default, Java should be available with Raspbian
## If not, manual Java Installation is needed
javaPath=`which javac`
if [ -z "$javaPath" ] ; then
  echo "Installing Open JDK-8" | tee -a $logFile
  sudo apt install openjdk-8-jdk openjdk-8-jre -qq --yes | tee -a $logFile
  javaPath=`which java`
fi
echo "Java is installed at $javaPath" | tee -a $logFile

## Check for Apache Maven
## If not installed yet, install Apache Maven from default Raspbian repository
mvnPath=`which mvn`
if [ -z "$mvnPath" ] ; then
  echo "Installing Apache Maven from the repository" | tee -a $logFile
  sudo apt-get install maven -qq --yes | tee -a $logFile
  mvnPath=`which mvn`
fi
echo "Apache Maven is installed at $mvnPath" | tee -a $logFile

## Check for Node JS
## If not installed yet, install Node JS from default Raspbian repository
nodePath=`which node`
if [ -z "$nodePath" ] ; then
  echo "Installing Node from raw.githubusercontent.com " | tee -a $logFile
  wget -O - https://raw.githubusercontent.com/sdesalas/node-pi-zero/master/install-node-v7.7.1.sh | bash | tee -a $logFile
  echo "Installing bleno package from npm " | tee -a $logFile
  sudo npm install bleno | tee -a $logFile
  nodePath=`which node`
fi
echo "Node is installed at $nodePath" | tee -a $logFile

## Check for Redis Server
## If not installed yet, install Redis Server from default Raspbian repository
redisPath=`which redis-server`
if [ -z "$redisPath" ] ; then
  echo "Installing Redis Server from the repository" | tee -a $logFile
  sudo apt-get install redis -qq --yes | tee -a $logFile
  redisPath=`which redis-server`
fi

## Start the Redis server, if not already running
redisRunning=`ps -aef | grep '^redis'`
if [ -z "$redisRunning" ] ; then
  echo "Starting Redis Server as background process..." | tee -a $logFile
  if [ -n "$redisPath" ] ; then
    $redisPath | tee -a $logFile &
    redisRunning=`ps -aef | grep '^redis'`
  else
    echo "Redis Server not available, exiting the install script" | tee -a $logFile
    exit 1
  fi
fi
echo $redisRunning | tee -a $logFile

# Setting up the bluetooth
echo "Setting up the Bluetooth for BLE Service" | tee -a $logFile
sudo systemctl stop bluetooth | tee -a $logFile
sudo systemctl disable bluetooth | tee -a $logFile

sudo hciconfig hci0 up | tee -a $logFile

sudo apt-get update -qq --yes | tee -a $logFile
sudo apt-get install -qq --yes bluetooth bluez libbluetooth-dev libudev-dev libcap2-bin | tee -a $logFile

## Go to BoT-Java-SDK Directory and build the jar
pwd | tee -a $logFile
mvn clean package | tee -a $logFile

## Check build is successfull and BoT-Java-SDK.jar is available in target
targetPath="`pwd`/target"
if [ $? -eq 0  -a -f "$targetPath/BoT-Java-SDK.jar" ] ; then
  ## Copy required setup files to target folder
  echo "Copying logback.xml, logging.properties and bleno-service.js to $targetPath" | tee -a $logFile
  cp src/main/resources/logback.xml src/main/resources/logging.properties src/main/resources/bleno-service.js $targetPath
  ls $targetPath | tee -a $logFile
  echo "BoT-Java-SDK Installation Completed" | tee -a $logFile
else
  echo "Building BoT-Java-SDK.jar failed, check $logFile for details and try again !!!" | tee -a $logFile
  exit 1
fi

echo "----------------------------------------" | tee -a $logFile
echo "|                 DONE                 |" | tee -a $logFile
echo "----------------------------------------" | tee -a $logFile
