FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD badword-0.0.1.jar badword.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/badword.jar"]