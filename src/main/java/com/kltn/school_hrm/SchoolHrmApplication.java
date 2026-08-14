package com.kltn.school_hrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SchoolHrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolHrmApplication.class, args);
	}

}
