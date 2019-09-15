#!/bin/bash

# Change this to your netid
netid=mcm140330

# Root directory of your project
PROJDIR=Proj1

# Directory where the config file is located on your local system
CONFIGLOCAL=config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=MainApp

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -lt $( echo $i | awk '{ print $1 }' ) ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        git-bash.exe -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host.utdallas.edu killall -u $netid" &

        n=$(( n + 1 ))
		sleep 1
    done
   
)


echo "Cleanup complete"
