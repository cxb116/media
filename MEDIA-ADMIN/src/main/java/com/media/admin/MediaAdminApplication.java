package com.media.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.media.admin.mapper")
public class MediaAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaAdminApplication.class, args);
        System.out.println("Media Admin Application Started Successfully!");
    }
}
