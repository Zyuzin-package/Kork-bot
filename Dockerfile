#FROM openjdk:17
#EXPOSE 8080
#ADD /target/KorkBotV2-1.0-SNAPSHOT.jar KorkBotV2-1.0-SNAPSHOT.jar
#ENTRYPOINT ["java","-jar","KorkBotV2-1.0-SNAPSHOT.jar"]
FROM openjdk:17
EXPOSE 8080
ADD /target/KorkBotV2-1.0-SNAPSHOT.jar /docker/bot.jar
ENTRYPOINT ["java","-jar","/docker/bot.jar"]