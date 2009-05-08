#! /bin/bash	
	
	if [[ `screen -ls | grep jproxy | wc -l` -ge 1 ]]; then { echo 'jproxy uruchomione'; }; else {
		echo 'jproxy start'
		screen -d -m -S jproxy sh -c "echo 'tail -f /home/dmn/Dropbox/Projects/jproxy/log.txt'; cd /home/dmn/Dropbox/Projects/jproxy ; ./start.sh > log.txt" &
	}; fi
	
