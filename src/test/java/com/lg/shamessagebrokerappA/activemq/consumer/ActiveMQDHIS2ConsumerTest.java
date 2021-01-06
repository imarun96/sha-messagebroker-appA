package com.lg.shamessagebrokerappA.activemq.consumer;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest
class ActiveMQDHIS2ConsumerTest {

    @SpyBean
    private ActiveMQDhis2Consumer dhis2Consumer;

    @Test
    void runActiveMQDhis2Consumer() {
        await().atMost(Duration.ONE_MINUTE).until(() -> {
            try {
                verify(dhis2Consumer, atLeast(1)).runConsumer();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
}