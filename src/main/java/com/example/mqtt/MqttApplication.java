package com.example.mqtt;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class MqttApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttApplication.class, args);
		log.info("mw");
	}

}
