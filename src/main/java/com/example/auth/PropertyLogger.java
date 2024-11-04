package com.example.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PropertyLogger implements CommandLineRunner {


    private static final Logger logger = LoggerFactory.getLogger(PropertyLogger.class);

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${rsa.private-key}")
    private String rsaPrivateKey;

    @Value("${spring.mail.host}")
    private  String mailHost;

    @Value("${spring.mail.port}")
    private  String mailPort;

    @Value("${spring.mail.username}")
    private  String mailUsername;

    @Value("${spring.mail.password}")
    private  String mailPassword;


    @Override
    public void run(String... args) {
        logger.info("""
                        
                        =======================
                        Application Name: {}
                        Datasource URL: {}
                        RSA Private Key Path: {}
                        ================================""",
                appName, datasourceUrl, rsaPrivateKey
                );

        logger.info("""
                ==========================
                Mail Host: {}
                Mail Port: {}
                Mail Username: {}
                Mail Password: {}
                ==========================""",
                mailHost, mailPort, mailUsername, mailPassword
                );
    }
}
