package com.gautam.ChatApplication;

import com.gautam.ChatApplication.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatApplication {

	public static void main(String[] args) {

		SpringApplication.run(ChatApplication.class, args);
	}

}
