package com.example.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
		"spring.application.name=${SPRING_APPLICATION_NAME:AuthApplication}"
})
class AuthApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull(System.getProperties());
	}

}
