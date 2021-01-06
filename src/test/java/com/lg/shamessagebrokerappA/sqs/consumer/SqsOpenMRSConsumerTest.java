package com.lg.shamessagebrokerappA.sqs.consumer;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.jms.JMSException;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ "sqs" })
class SqsOpenMRSConsumerTest {

    @SpyBean
    private SqsOpenMRSConsumer openMRSConsumer;

    @Test
    void runSqsOpenMRSConsumer() {
        await().atMost(Duration.FIVE_MINUTES).until(() -> {
            try {
                verify(openMRSConsumer, atLeast(1)).runConsumer();
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}