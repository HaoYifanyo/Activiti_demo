package com.hao.activiti_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,org.activiti.spring.boot.SecurityAutoConfiguration.class})
public class ActivitiDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActivitiDemoApplication.class, args);
	}

}
