#!/bin/bash

echo "checking user..."
WHOAMI=`exec <&- 2>&-; which whoami || type whoami`
WHO=`$WHOAMI`
if [ "$WHO" = "root" ]
then 
	echo "You are superuser,dfs can not be executed by root."
	exit
else 
	echo "Done."
fi

# source jdk env
if [ -d "/usr/local/src/jdk1.8.0_301" ] ; then
	export JAVA_HOME=/usr/local/src/jdk1.8.0_301
	export JRE_HOME=$JAVA_HOME/jre
	export CLASSPATH=$JAVA_HOME/lib:$CLASSPATH
	export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
fi

cd ~/atmp/

date

JAR_NAME=atmp.jar
APP_NAME=atmp

PIDS=`ps -ef | grep ${JAR_NAME} | grep -v grep | awk '{print $2}'`
if [ "$PIDS" != "" ]; then
    echo $APP_NAME is running on $PIDS
else
	echo start portal
	nohup java -jar ${JAR_NAME} >> /dev/null 2>&1 &
	sleep 5
	PID=`ps -ef | grep -w ${JAR_NAME} | grep -v grep | awk '{ print $2 }'`
	if [ ! $PID ]
	then
		echo $APP_NAME is not starting
	else
		echo $APP_NAME is running on $PID
	fi
fi

echo
