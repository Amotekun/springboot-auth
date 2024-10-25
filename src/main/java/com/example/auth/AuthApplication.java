package com.example.auth;

import com.example.auth.config.RsaKeyProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import io.github.cdimascio.dotenv.Dotenv;


@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class AuthApplication {

	public static void main(String[] args) {

		// Load dotenv to read environment variables
		Dotenv dotenv = Dotenv.configure()
				.directory("./") // Adjust directory if needed
				.load();

		String appName = dotenv.get("SPRING_APPLICATION_NAME");
		if (appName != null) {
			System.setProperty("SPRING_APPLICATION_NAME", appName);
		} else {
			System.err.println("SPRING_APPLICATION_NAME not found in .env");
			System.exit(1); // Ensure the app stops if the property isn't found
		}

		SpringApplication.run(AuthApplication.class, args);
	}

}
