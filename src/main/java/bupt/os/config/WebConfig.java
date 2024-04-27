package bupt.os.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:63342") // 允许来自这个域的访问
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE") // 允许的HTTP方法
                .allowedHeaders("Content-Type"); // 允许的头部
    }
}
