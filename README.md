# Mutual-Exclusion-Algorithm
Roucairol and Carvalho's distributed mutual exclusion algorithm

The source code is at Proj3/src
Proj3/config.txt includes a custom config file we used to test the code
A brief description of the algorithm is included in Description.pdf


1. Save "Proj3" under your home directory in the dc servers and modify config.txt in Proj3 to the one in your local machine (they must match)
	NOTE: nodes start at node 0 and increment from there
2. Navigate to ~ /Proj3/src
3. Run javac *.java to compile
4. Edit the launcher and cleanup shell scripts to the proper information that will work for you (netid, terminal used, location of config.txt etc..)
	set The MSGS variable to the number of messages each node should broadcast.
5. Setup passwordless login as in the tutorial provided by TA
6. Run launcherNETWORK.sh