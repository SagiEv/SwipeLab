package com.swipelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class SwipeLabApplication {
	public static void main(String[] args) {
		SpringApplication.run(SwipeLabApplication.class, args);
	}

}