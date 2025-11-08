package com.quetoquenana.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.quetoquenana.userservice.config")
public class UserServiceApplication {

    public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
