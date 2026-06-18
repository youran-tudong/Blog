package com.technote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.technote.**.mapper")
@SpringBootApplication
public class TechNoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechNoteApplication.class, args);
    }
}

