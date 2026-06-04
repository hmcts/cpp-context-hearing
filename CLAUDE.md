# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build
```bash
mvn clean install -nsu
```

### Run unit tests
```bash
# All unit tests
mvn test

# Single test class
mvn test -Dtest=ClassName

# Single test method
mvn test -Dtest=ClassName#methodName
```

### Run integration tests
```bash
cd hearing-integration-test
mvn verify -Phearing-integration-test

# Single integration test class
mvn verify -Phearing-integration-test -Dit.test=TestClassName
```

### Build with Sonar analysis
```bash
mvn -C -U verify sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.issuesReport.html.enable=true -Dsonar.exclusions=target/generated-sources/**
```

## Architecture

This is an **event-sourced CQRS microservice** for the Ministry of Justice's Common Platform Project (CPP). It manages hearing lifecycle operations: applications, cases, defendants, counsel, witnesses, and scheduling.

### CQRS + Event Sourcing Pattern

The service strictly separates commands (writes) from queries (reads):

- **Commands** mutate state by appending domain events to an event store
- **Event processors** consume events and build projections into the view store
- **Queries** read directly from the view store (denormalized read model)

### Module Structure

```
hearing-command/
  hearing-command-api/        # JAX-RS REST endpoints that accept commands
  hearing-command-handler/    # ~30 command handlers (business logic + event publishing)

hearing-query/
  hearing-query-api/          # JAX-RS REST endpoints that return query results
  hearing-query-view/         # Query result formatting/projection

hearing-domain/
  hearing-domain-aggregate/   # Domain aggregates (event-sourced state reconstruction)
  hearing-domain-common/      # Shared value objects and domain types
  hearing-domain-event/       # Domain event definitions (what happened)
  hearing-domain-xhibit/      # Xhibit integration domain types

hearing-event/
  hearing-event-listener/     # Subscribes to the event stream
  hearing-event-processor/    # Projects events into the view store

hearing-viewstore/
  hearing-viewstore-persistence/  # JPA entities + repositories for the read model

hearing-event-sources/        # Infrastructure: event store connection
hearing-common/               # Cross-cutting utilities
hearing-healthchecks/         # Health check endpoints
hearing-json/                 # JSON serialization helpers
hearing-service/              # Assembles all modules into a deployable WAR (WildFly)

hearing-integration-test/     # Integration tests (Failsafe, WildFly embedded, Rest-assured)
test-utilities/               # Shared test matchers and fixtures
pojo-plugin/                  # Maven plugin for POJO code generation
```

### Data Flow

1. Client sends HTTP request → **Command API** (hearing-command-api)
2. Command handler loads aggregate from event store, validates, publishes domain events → **Command Handler** (hearing-command-handler)
3. Event listener receives events → **Event Processor** builds view store projection
4. Client queries → **Query API** reads from **View Store**

### Key Technologies

- **Runtime**: Java 17, WildFly application server, Docker
- **Database**: PostgreSQL, schema managed by Liquibase in `hearing-viewstore`
- **Framework**: `uk.gov.moj.cpp.common:service-parent-pom` (HMCTS CPP internal framework for event sourcing and CQRS)
- **Testing**: JUnit, Rest-assured (HTTP), Awaitility (async), Wiremock (stubbing), Testcontainers
- **CI/CD**: Azure DevOps (`azure-pipelines.yaml`) — runs Sonar on PRs, full integration tests on `main` and `team/*` branches

### Adding New Command Handlers

Command handlers live in `hearing-command/hearing-command-handler/src/main/java`. Each handler implements the framework's command handler interface, loads the aggregate, applies business logic, and publishes events. The corresponding domain events are defined in `hearing-domain/hearing-domain-event`.

### Database Migrations

Liquibase changesets are in `hearing-viewstore/hearing-viewstore-persistence/src/main/resources`. These run automatically on deployment via the `hearing-viewstore-liquibase` module bundled into the WAR.

### Integration Tests

Integration tests in `hearing-integration-test/` use a profile `hearing-integration-test` and follow the `*IT.java` naming convention. They spin up a WildFly embedded container and test the full HTTP → command → event → view store flow.
