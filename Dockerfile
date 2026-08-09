# ---------- build stage ----------
# The Maven image is used rather than ./mvnw on purpose: this repo is developed on
# Windows, so the wrapper script can carry CRLF line endings, which a Linux shell
# rejects with a confusing "/bin/sh^M: bad interpreter". Using mvn directly sidesteps it.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Dependencies are resolved in their own layer, keyed only on pom.xml. Docker reuses
# that layer on every build where the pom has not changed, so editing Java code does
# not re-download the internet.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

# Tests are skipped HERE and run in CI instead. The integration tests start Docker
# containers, which they cannot do from inside a Docker build.
RUN mvn -B clean package -DskipTests

# ---------- runtime stage ----------
# A JRE, not a JDK: the compiler and build tooling are dead weight (and extra attack
# surface) in a running container. This is the whole point of a multi-stage build —
# the final image contains the jar and a Java runtime, nothing that built it.
FROM eclipse-temurin:21-jre-noble AS runtime

# Run as a non-root user. If the app is ever compromised, the attacker lands as a
# user that owns nothing rather than as root inside the container.
RUN useradd --system --create-home --uid 1001 wordsmith
USER wordsmith

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage matters on small instances: the JVM's default heap sizing is
# conservative, and on a 512 MB box it leaves a lot of memory unused.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
