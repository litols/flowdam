#!/bin/sh
### BEGIN INIT INFO
# Provides:          stopcock
# Required-Start:    $local_fs $remote_fs $network $syslog
# Required-Stop:     $local_fs $remote_fs $network $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop stopcock server
### END INIT INFO

case $1 in
    start)
        echo "Starting stopcock ..."
        if [ ! -f /var/run/stopcock.pid ]; then
            cd /home/stopcock
            nohup java -Dlog4j.configurationFile=/home/stopcock/log4j2.xml -jar /home/stopcock/stopcock-0.0.1-SNAPSHOT.jar 2>> /dev/null >> /dev/null &
            echo $! > /var/run/stopcock.pid
            echo "stopcock started ..."
        else
            echo "stopcock is already running ..."
        fi
    ;;
    stop)
        if [ -f /var/run/stopcock.pid ]; then
            PID=$(cat /var/run/stopcock.pid);
            echo "Stopping stopcock ..."
            kill $PID;
            echo "stopcock stopped ..."
            rm /var/run/stopcock.pid
        else
            echo "stopcock is not running ..."
        fi
    ;;
    restart)
        if [ -f /var/run/stopcock.pid ]; then
            PID=$(cat /var/run/stopcock.pid);
            echo "Stopping stopcock ...";
            kill $PID;
            echo "stopcock stopped ...";
            rm /var/run/stopcock.pid

            echo "Starting stopcock ..."
            cd /home/stopcock
            nohup java -Dlog4j.configurationFile=/home/stopcock/log4j2.xml -jar /home/stopcock/stopcock-0.0.1-SNAPSHOT.jar 2>> /dev/null >> /dev/null &
            echo $! > /var/run/stopcock.pid
            echo "stopcock started ..."
        else
            echo "stopcock is not running ..."
        fi
    ;;
esac