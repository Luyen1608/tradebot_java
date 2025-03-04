package luyen.tradebot.Trade.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.List;


//cách một
@Configuration
public class AppConfig  implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("**")
                .allowCredentials(true)
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}


//cách 2 tạo bean

//@Configuration
//public class AppConfig   {
//   @Bean
//    public WebMvcConfigurer corsConfigurer() {
//       return new WebMvcConfigurer() {
//           @Override
//           public void addCorsMappings(CorsRegistry registry) {
//               registry.addMapping("/**")
//                .allowCredentials(true)
//                .allowedOrigins("http://localhost:8080")
//                .allowedMethods("*")
//                .allowedHeaders("*");
//           }
//       };
//   }
//}

//Cách 3 dùng filte
//@Configuration
//public class AppConfig   {
//   @Bean
//    public FilterRegistrationBean<CorsFilter> corsFilter(){
//       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//       CorsConfiguration config = new CorsConfiguration();
//       config.setAllowCredentials(true);
//       config.setAllowedOrigins(List.of("http://localhost:8080","http://localhost:8081"));
//       config.addAllowedHeader("*");
//       config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//       source.registerCorsConfiguration("/user/**", config);
//       FilterRegistrationBean bean = new FilterRegistrationBean<>(new CorsFilter(source));
//       bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//       return bean;
//
//   }
//}



//Cách 4 dùng do fillter
//@Component
//public class AppConfig extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
//        filterChain.doFilter(request, response);
//    }
//}

