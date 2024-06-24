package com.lld.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lld.message.dao.mapper")
public class MsgStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsgStoreApplication.class, args);
    }


}


