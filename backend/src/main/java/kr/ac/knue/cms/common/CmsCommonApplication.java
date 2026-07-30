package kr.ac.knue.cms.common;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kr.ac.knue.cms.common")
public class CmsCommonApplication {
  public static void main(String[] args) {
    SpringApplication.run(CmsCommonApplication.class, args);
  }
}
