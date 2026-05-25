package ynu.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有接口跨域
        registry.addMapping("/**")  // 匹配前端请求的接口路径
                .allowedOrigins("http://localhost:5173")  // 前端项目运行的域名+端口
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600)    // 预检请求缓存时间（秒）.allowedOrigins("http://localhost:5173")  // 前端项目运行的域名+端口
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }
}
