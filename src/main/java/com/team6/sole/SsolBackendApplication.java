package com.team6.sole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching		// 캐싱
@EnableScheduling	// 스케줄러
public class SsolBackendApplication extends SpringBootServletInitializer {

	// ec2 404에러 해결 코드
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SsolBackendApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SsolBackendApplication.class, args);
	}

}
