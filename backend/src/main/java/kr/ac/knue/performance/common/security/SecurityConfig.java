package kr.ac.knue.performance.common.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
  @Bean
  FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthenticationFilterRegistration(SessionAuthenticationFilter filter) {
    FilterRegistrationBean<SessionAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
    bean.setOrder(20);
    bean.addUrlPatterns("/api/*");
    return bean;
  }
}
