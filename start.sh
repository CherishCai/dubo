#########################################################################
# File Name: start.sh
# Author: Cherish
# mail: 785427346@qq.com
# Created Time: 2018年04月26日 星期四 23时40分25秒
#########################################################################
#!/bin/bash

git pull

mvn clean package -Dmaven.test.skip

pkill -9 -f dubo

nohup java -jar target/dubo.jar &
