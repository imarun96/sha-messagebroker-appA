package com.lg.shamessagebrokerappA.sqs.consumer;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.hc.core5.http.ParseException;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ "sqs" })
class SqsDHIS2ConsumerTest {

    @SpyBean
    private SqsDHIS2Consumer dhis2Consumer;

    @Test
    void runSqsDhis2Consumer() {
        await().atMost(Duration.FIVE_MINUTES).until(() -> {
            try {
                verify(dhis2Consumer, atLeast(1)).runConsumer();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}