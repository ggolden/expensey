FROM openjdk:8-alpine

ADD target/app-latest.jar /home/expensey/expensey.jar
ADD config.yml /home/expensey/config.yml

WORKDIR /home/expensey/
CMD java -XshowSettings -verbose:gc -XX:+PrintGCTimeStamps  -XX:+PrintGCDetails -Xloggc:/logs/expensey_gc.log -XX:-UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=1000K -jar expensey.jar server config.yml >> /logs/expensey_out.log 2>&1
EXPOSE 8080
