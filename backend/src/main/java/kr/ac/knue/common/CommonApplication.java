package kr.ac.knue.common;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kr.ac.knue.common")
public class CommonApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommonApplication.class, args);
  }
}
