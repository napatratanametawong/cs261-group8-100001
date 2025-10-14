# -------- Build WAR --------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# -------- Runtime: External Tomcat --------
FROM tomcat:10.1.44-jre17
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

ENV TZ=Asia/Bangkok \
    CATALINA_OPTS="-Xms256m -Xmx1024m"
EXPOSE 8080
CMD ["catalina.sh","run"]
