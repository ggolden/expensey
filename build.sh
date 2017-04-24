cd /home \
	&& git clone https://github.com/ggolden/expensey.git \
	&& cd expensey \
	&& mvn clean install
	
# cd /home && java -XshowSettings -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:/home/logs/expensey_gc.log -XX:-UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=1000K -jar /home/expensey/app/target/app-latest.jar server /home/expensey/app/config.yml >> /home/logs/expensey_out.log 2>&1
