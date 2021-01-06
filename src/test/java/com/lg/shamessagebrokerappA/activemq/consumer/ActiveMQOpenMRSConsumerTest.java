package com.lg.shamessagebrokerappA.activemq.consumer;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.jms.JMSException;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class ActiveMQOpenMRSConsumerTest {

    @SpyBean
    private ActiveMQOpenMRSConsumer openMRSConsumer;

    @Test
    void runActiveMQOpenMRSConsumer() {
        await().atMost(Duration.TWO_MINUTES).until(() -> {
            try {
                verify(openMRSConsumer, atLeast(1)).runConsumer();
            } catch (JMSException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}