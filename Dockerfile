# Use a slim JDK image
FROM eclipse-temurin:17-jdk

WORKDIR /

COPY lib/mysql-connector-j-9.6.0.jar /lib
COPY src/main/java /src
COPY public /public

RUN mkdir out && \
    find src -name "*.java" > sources.txt && \
    javac -cp "lib/*" -d out @sources.txt && \
    rm sources.txt

EXPOSE 8080

CMD ["java", "-cp", "out:lib/*", "main.java.com.carrental.CarRentalApplication"]