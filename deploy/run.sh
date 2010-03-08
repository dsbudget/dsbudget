#!/bin/bash
if [ ! $JAVA_HOME ]; then
	echo "JAVA_HOME is not set.. please install SUN Java 6 (not Open-JDK!), then add something like following to your ~/.profile"
	echo "export JAVA_HOME=/usr/lib/jvm/default-java"
	exit
fi

$JAVA_HOME/bin/java -jar dsbudget.jar &
