package com.ssafy.anymeetsong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://i6a505.p.ssafy.io")
				.allowedOrigins("http://localhost")
				.allowedOrigins("https://i6a505.p.ssafy.io")
				.allowedOrigins("https://localhost")
				.allowedOrigins("*");
//				.allowCredentials(true);
	}
}
