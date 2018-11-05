package ru.apolyakov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@SpringBootApplication
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        CloudFoundryService cloudFoundryService = ctx.getBean(CloudFoundryService.CLOUD_FOUNDRY_SERVICE_NAME, CloudFoundryService.class);
        cloudFoundryService.auth();
    }
}
