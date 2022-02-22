echo "Running Liquibase"
dbServerName=$1
dbUserName=$2
dbPassword=$3
java -jar event-repository-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingeventstore?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar aggregate-snapshot-repository-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingeventstore?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar event-buffer-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingviewstore?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar event-tracking-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingviewstore?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar hearing-viewstore-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingviewstore?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar framework-system-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingsystem?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi
java -jar activiti-liquibase.jar --url=jdbc:postgresql://${dbServerName}:5432/hearingactiviti?sslmode=require --username=${dbUserName} --password=${dbPassword} --logLevel=info update
if [ $? -ne 0 ]
then
    exit 1
else
    echo success!
fi