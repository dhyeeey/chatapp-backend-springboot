package com.dhyey.chatapp_withjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChatappWithjpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatappWithjpaApplication.class, args);
	}

}
