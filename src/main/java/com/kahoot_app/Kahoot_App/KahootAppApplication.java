package com.kahoot_app.Kahoot_App;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KahootAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(KahootAppApplication.class, args);
	}

}
