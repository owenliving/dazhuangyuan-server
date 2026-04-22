package com.dazhuangyuan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dazhuangyuan.mapper")
public class DazhuangyuanApplication {

    public static void main(String[] args) {
        SpringApplication.run(DazhuangyuanApplication.class, args);
        System.out.println("========================================");
        System.out.println("  大状元高考志愿填报系统 启动成功!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("  API文档: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
