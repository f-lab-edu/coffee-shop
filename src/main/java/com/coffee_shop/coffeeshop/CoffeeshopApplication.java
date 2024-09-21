package com.coffee_shop.coffeeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CoffeeshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeshopApplication.class, args);
	}

}
