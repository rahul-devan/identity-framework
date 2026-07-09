package com.ndash.identity_framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdentityFrameworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityFrameworkApplication.class, args);
	}

}
