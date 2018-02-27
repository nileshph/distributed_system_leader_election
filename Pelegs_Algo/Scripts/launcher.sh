#!/bin/bash

# Change this to your netid
netid=nxp161330

# Root directory of your project
PROJDIR=/people/cs/s/sxg122830/TestProj

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=HelloWorld

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
	
	osascript -e 'tell app "Terminal"
        do script "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no '$netid@$host' java -cp '$BINDIR' '$PROG' '$p'; '$SHELL'"
    end tell'

        n=$(( n + 1 ))
    done
)
