FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} api-service.jar

ENTRYPOINT ["java","-jar", "api-service.jar"]

#hot trên cổng đang chạy ứng dụng
EXPOSE 8086