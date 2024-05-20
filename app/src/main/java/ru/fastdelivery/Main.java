package ru.fastdelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Класс запускающий приложение
 */
@SpringBootApplication(scanBasePackages = { "ru.fastdelivery" })
@ConfigurationPropertiesScan(value = { "ru.fastdelivery.properties", "ru.fastdelivery.presentation" })
@EnableConfigurationProperties
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}