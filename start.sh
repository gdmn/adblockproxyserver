#! /bin/bash
cd dist/

#java -jar jproxy.jar --port 11180 --block "http://www.niecko.pl/adblock/adblock.txt" "http://jurek6.republika.pl/adblockplus.txt" "file:../adblockchewey.txt"

#--port 11180 --block "file:/home/dmn/Dropbox/Projects/jproxy/moje.adblock.txt" "file:/home/dmn/Dropbox/Projects/jproxy/wwwnieckopl.adblock.txt"

java -jar jproxy.jar --debug 2 --threads 24 --port 11180 --block "file:../moje.adblock.txt" "file:../jurek6republikapl.adblock.txt" "file:../wwwnieckopl.adblock.txt" "file:../cheweyde.adblock.txt"
cd ..

