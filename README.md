# ReservationService

## Building the Service
- `./gradlew build` runs tests and builds the application
- `./gradlew tasks` shows all available tasks

JDK 17 is required to build and run this application
as well as a postgresql instance. Gradle should download the
appropriate JDK if a suitable version isn't found on the machine,
but it isn't always able to do that.

The app will create the schema, but you'll need to
provide a database connection. H2 or PostgreSQL should work
okay.

### Container
I didn't use it during the bulk of development, but to make running/evaluating easier, I added a docker compose configuration, so `docker compose up` will setup postgres and the service for requests on `localhost:9090` by default.

## Design

### Application
This application runs as a typical Java Virtual Machine application.

The main function and server configuration can be found in [Application.kt](src/main/kotlin/com/jzbrooks/reservations/Application.kt)
Route configuration can be found in [Routing.kt](src/main/kotlin/com/jzbrooks/reservations/Routing.kt)

### Controller
[`Controller`](src/main/kotlin/com/jzbrooks/reservations/controllers/Controller.kt) is responsible for request validation and HTTP semantics
beyond routing. Unit tests are found in [`ControllerTest`](src/test/kotlin/com/jzbrooks/reservations/controllers/ControllerTest.kt).
This is slightly less than ideal since HTTP verbs usually imply a certain set of successful response status codes and the Routing/Controller
separation means those two concerns are uncoupled, but testing request validation seemed valuable. This is an area the service could improve.

### Repository
[`Repository`](src/main/kotlin/com/jzbrooks/reservations/data/Repository.kt) is responsible for data management.
The only real implementation (i.e. not a test fake) is [`SqlRepository`](src/main/kotlin/com/jzbrooks/reservations/data/SqlRepository.kt),
which configures a jdbc driver and connection string. Database-level logic is tested in [`SqlRepositoryTest`](src/test/kotlin/com/jzbrooks/reservations/data/SqlRepositoryTest.kt),
which uses H2 in-memory. This is not a perfect stand-in for PostgreSQL, but it should be fine since an ORM is in place
that should execute appropriate SQL for each DBMS underneath.

### Shortcuts
- A nice dependency injection system like Dagger would benefit the codebase as it grows, encouraging loose coupling between classes, encouraging better flexibility and testability.
- Dates and times are represented by non-standard convention. ISO 8601 would make for a nicer API surface.
- Dates and times have no concept of timezones or non-standard calendars.
- Some simple execution plans were run, but there's quite possibly room for improvement in the database schema or queries. I learned Postgres' optimizer tends to prefer full table scans over an index scan for small data sets.
- JetBrain's Exposed ORM for Kotlin [doesn't support all SQL operations](https://github.com/JetBrains/Exposed/wiki/FAQ). I don't love the DSL, either. [SQLDelight](https://github.com/cashapp/sqldelight) is a pretty sharp too, but it's Postgres support is experimental _and_ a required package for the dialect [doesn't seem to be published to the standard package repository](https://central.sonatype.com/search?q=sqldelight%2520postgres-dialect).
- I suspect that some queries could be improved or combined to better leverage DBMS features for performance and scalability gains.
- Optimizing the final jar with R8 would be nice.
- Running tests when building the docker image would be a good idea.
- GET endpoints would benefit from query parameters for date ranges, paging, and perhaps others.
