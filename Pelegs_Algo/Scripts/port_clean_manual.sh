##script to kill process using node specific ports
echo $(lsof -n -i4TCP:1233)
kill -9 $(lsof -n -i4TCP:1233)

echo $(lsof -n -i4TCP:1234)
kill -9 $(lsof -n -i4TCP:1234)

echo $(lsof -n -i4TCP:1235)
kill -9 $(lsof -n -i4TCP:1235)

echo $(lsof -n -i4TCP:1236)
kill -9 $(lsof -n -i4TCP:1236)

echo $(lsof -n -i4TCP:1237)
kill -9 $(lsof -n -i4TCP:1237)