@echo off

cd dist/
java -jar jproxy.jar --port 11180 --threads 20 --debug 1 --block "file:../moje.adblock.txt" "file:../jurek6republikapl.adblock.txt" "file:../wwwnieckopl.adblock.txt" "file:../cheweyde.adblock.txt"
cd ..
