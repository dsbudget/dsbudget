#!/bin/bash

#if [ ! $JAVA_HOME ]; then
#	echo "JAVA_HOME is not set.. please install SUN Java 6 (not Open-JDK!), then add something like following to your ~/.profile" | tee booterror.txt
#	echo "export JAVA_HOME=/usr/lib/jvm/default-java" | tee -a booterror.txt
#	exit
#fi

java -Ddivrep_invalidate_samepagekey -jar dsbudget.jar & 2> booterror.txt
