package com.lg.shamessagebrokerappA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(exclude = { ContextStackAutoConfiguration.class })
@EnableScheduling
@RefreshScope
@EnableJms
@Slf4j
public class ShaMessagebrokerAppAApplication {

    public static void main(String[] args) {
        log.info("ShaMessagebrokerAppA Service started.");
        SpringApplication.run(ShaMessagebrokerAppAApplication.class, args);
    }
}