# ============================
# Stage 1: Build (Maven)
# ============================
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app

# 先複製 pom.xml 以利用 Docker 快取
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 複製原始碼並打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================
# Stage 2: Runtime (Tomcat)
# ============================
FROM tomcat:11.0-jdk11-temurin

# 移除 Tomcat 預設首頁
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# 從 build 階段複製 WAR 檔
COPY --from=build /app/target/seat-lottery.war /usr/local/tomcat/webapps/seat-lottery.war

# 複製 entrypoint 腳本
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["catalina.sh", "run"]
