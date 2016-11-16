To Update the database

mvn -Dliquibase.url=jdbc:postgresql://localhost:5432/hearing-view-store-db -Dliquibase.username=postgres -Dliquibase.password=postgres -Dliquibase.logLevel=info resources:resources liquibase:update