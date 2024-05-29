package com.example.cfft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.cfft.mapper") // 扫描Mapper接口")
public class CfftApplication{

    public static void main(String[] args) {
        SpringApplication.run(CfftApplication.class, args);
    }

}
