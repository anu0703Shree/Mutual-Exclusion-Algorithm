#!/bin/bash

# Change this to your netid
netid=mcm140330

# Root directory of your project
PROJDIR=Proj3

# Directory where the config file is located on your local system
CONFIGLOCAL=config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/src

# Your main project class
PROG=MainApp



n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -lt $( echo $i | awk '{ print $1 }' ) ]]
    do
		echo $n
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
		port=$( echo $line | awk '{ print $3 }' )
		
	echo $line
	#change "git-bash.exe -c" to your preferred terminal
	git-bash.exe -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host.utdallas.edu java -cp $BINDIR $PROG $p $host $port; exec bash" &
        n=$(( n + 1 ))
    done
)
sleep 1