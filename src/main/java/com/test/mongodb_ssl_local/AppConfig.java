package com.test.mongodb_ssl_local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${connectionString}")
    public static String connectionString;

    @Value("${clientJksFilePath}")
    public static String clientJksFilePath;

    @Value("${jksPassword}")
    public static String jksPassword;

    @Value("${rootPassword}")
    public static String rootPassword;

    @Value("${caPemFilePath}")
    public static String caPemFilePath;

    @Value("${rootUserName}")
    public static String rootUserName;
}
