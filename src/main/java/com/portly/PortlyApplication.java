package com.portly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PortlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortlyApplication.class, args);
	}

}
