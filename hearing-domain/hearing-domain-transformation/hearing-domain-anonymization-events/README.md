To run

touch processfile

java -jar -Dorg.wildfly.swarm.mainProcessFile=./processfile
-Devent.transformation.jar=./hearing-domain-anonymization-events-2.5.48-CC-SNAPSHOT.jar
./event-tool-5.2.1-swarm.jar -c ./standalone-ds.xml -DstreamCountReportingInterval=1000

To debug

touch processfile

java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
-Dorg.wildfly.swarm.mainProcessFile=./processfile
-Devent.transformation.jar=./hearing-domain-anonymization-events-2.5.48-CC-SNAPSHOT.jar
./event-tool-5.2.1-swarm.jar -c ./standalone-ds.xml -DstreamCountReportingInterval=1000


