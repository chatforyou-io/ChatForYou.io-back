package com.chatforyou.io;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatforyouApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatforyouApplication.class, args);
	}

}
