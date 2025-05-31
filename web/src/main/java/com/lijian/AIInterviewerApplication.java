// web/src/main/java/com/lijian/AIInterviewerApplication.java
package com.lijian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lijian.**.mapper")
public class AIInterviewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIInterviewerApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    AI面试后端系统启动成功！");
        System.out.println("    访问地址: http://localhost:8083");
        System.out.println("=======================================");
    }
}