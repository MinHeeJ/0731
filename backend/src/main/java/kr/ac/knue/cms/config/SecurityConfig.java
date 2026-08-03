package kr.ac.knue.cms.config;

import kr.ac.knue.cms.auth.SessionAuthenticationInterceptor;
import kr.ac.knue.cms.auth.SessionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    private final ObjectProvider<SessionAuthenticationInterceptor> interceptor;
    public SecurityConfig(ObjectProvider<SessionAuthenticationInterceptor> interceptor) { this.interceptor = interceptor; }
    @Override public void addInterceptors(InterceptorRegistry registry) { interceptor.ifAvailable(i -> registry.addInterceptor(i).addPathPatterns("/api/**")); }
    @Bean
    @ConditionalOnBean(SessionService.class)
    SessionAuthenticationInterceptor sessionAuthenticationInterceptor(SessionService sessionService, ObjectMapper objectMapper) { return new SessionAuthenticationInterceptor(sessionService, objectMapper); }
}
