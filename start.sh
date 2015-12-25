nohup java -jar pnb-0.0.1-SNAPSHOT.jar > log.txt 2> errors.txt < /dev/null &
PID=$!
echo $PID > pid.txt
