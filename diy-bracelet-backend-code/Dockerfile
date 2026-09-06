# 多阶段构建,第一阶段:构建应用
FROM maven:3.8.8-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom 文件
COPY pom.xml .
COPY diy-common/pom.xml diy-common/
COPY diy-pojo/pom.xml diy-pojo/
COPY diy-server/pom.xml diy-server/

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY diy-common/src diy-common/src
COPY diy-pojo/src diy-pojo/src
COPY diy-server/src diy-server/src

# 构建应用，跳过测试
RUN mvn clean package -DskipTests -B

# 第二阶段：运行应用
FROM openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 jar 文件
COPY --from=builder /app/diy-server/target/*.jar app.jar

# 复制微信支付证书文件
COPY certs /app/certs

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口（根据你的 application.yml 配置调整）
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 配置 JVM 参数和应用配置
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
    -Dsky.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
    -Dsky.datasource.host=10.38.100.245 \
    -Dsky.datasource.port=3306 \
    -Dsky.datasource.database=diy_bangle \
    -Dsky.datasource.username=tang \
    -Dsky.datasource.password=Tang20030209"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
