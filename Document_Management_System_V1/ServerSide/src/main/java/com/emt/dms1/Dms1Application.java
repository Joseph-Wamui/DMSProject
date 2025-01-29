package com.emt.dms1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Dms1Application {

	public static void main(String[] args) {
		SpringApplication.run(Dms1Application.class, args);
	}

}