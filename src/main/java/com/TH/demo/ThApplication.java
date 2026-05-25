package com.TH.demo;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ThApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThApplication.class, args);
	}

}
//netstat -ano | findstr :8080
//taskkill /PID 8080 /F
