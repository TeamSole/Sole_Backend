package com.team6.sole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
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
