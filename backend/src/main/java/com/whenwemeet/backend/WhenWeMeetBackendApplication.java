package com.whenwemeet.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WhenWeMeetBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhenWeMeetBackendApplication.class, args);
	}

}
