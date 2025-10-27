# -------- Build WAR --------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# -------- Runtime: Tomcat --------
FROM tomcat:10.1.44-jre17
# ลบแอประเริ่มต้นออก
RUN rm -rf /usr/local/tomcat/webapps/*
# คัดลอก WAR เป็น ROOT.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# === Pull TU server cert during build (no repo files needed) ===
# You can override TU_HOST at build time if needed
ARG TU_HOST=restapi.tu.ac.th
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates openssl curl; \
    update-ca-certificates; \
    # get server cert (first cert in chain)
    echo | openssl s_client -showcerts -servername "$TU_HOST" -connect "$TU_HOST:443" 2>/dev/null \
      | openssl x509 -outform PEM > /tmp/tu-server.crt; \
    # import to JVM truststore (Temurin path)
    keytool -delete -alias tu-server -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit || true; \
    keytool -importcert -noprompt -trustcacerts \
      -alias tu-server \
      -file /tmp/tu-server.crt \
      -keystore "$JAVA_HOME/lib/security/cacerts" \
      -storepass changeit; \
    rm -f /tmp/tu-server.crt; \
    rm -rf /var/lib/apt/lists/*

# runtime opts
ENV TZ=Asia/Bangkok \
    CATALINA_OPTS="-XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# simple health endpoint (adjust to your app)
HEALTHCHECK --interval=20s --timeout=5s --retries=5 CMD curl -fsS http://localhost:8080/ || exit 1

CMD ["catalina.sh","run"]
