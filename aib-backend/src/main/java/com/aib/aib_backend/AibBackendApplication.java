package com.aib.aib_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.aib.aib_backend.model")  // ← Add this!
@EnableJpaRepositories("com.aib.aib_backend.repository")  // ← Add this!
public class AibBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AibBackendApplication.class, args);
	}
}