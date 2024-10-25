package com.example.auth.controllers;

import com.example.auth.config.SecurityConfig;
import com.example.auth.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({UserControllers.class,})
@Import({SecurityConfig.class, TokenService.class})
class UserControllersTest {

    @Autowired
    MockMvc mockMvc;


}