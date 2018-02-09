##script to kill process using node specific ports
echo $(lsof -n -i4TCP:3332)
kill -9 $(lsof -n -i4TCP:3332)

echo $(lsof -n -i4TCP:5678)
kill -9 $(lsof -n -i4TCP:5678)

echo $(lsof -n -i4TCP:5231)
kill -9 $(lsof -n -i4TCP:5231)

echo $(lsof -n -i4TCP:2311)
kill -9 $(lsof -n -i4TCP:2311)

echo $(lsof -n -i4TCP:3124)
kill -9 $(lsof -n -i4TCP:3124)

