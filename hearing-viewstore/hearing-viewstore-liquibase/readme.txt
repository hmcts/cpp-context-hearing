To Update the database

mvn -Dliquibase.url=jdbc:postgresql://localhost:5432/hearingviewstore -Dliquibase.username=hearing -Dliquibase.password=hearing -Dliquibase.logLevel=info resources:resources liquibase:update