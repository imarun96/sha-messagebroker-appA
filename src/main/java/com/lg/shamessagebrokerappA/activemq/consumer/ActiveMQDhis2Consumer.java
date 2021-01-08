package com.lg.shamessagebrokerappA.activemq.consumer;

import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;

import lombok.extern.slf4j.Slf4j;

/*
 * Consumes DHIS2 message from ActiveMQ
 */

@Component
@Profile("activemq")
@Slf4j
public class ActiveMQDhis2Consumer {

    @Value("${activeMQ.brokerURL}")
    String brokerURL;

    @Value("${activeMQ.uname}")
    String uname;

    @Value("${activeMQ.password}")
    String password;

    @Value("${activeMQ.targetDHIS2Queue}")
    String targetDHIS2Queue;

    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;
    private static final Integer RECEIVING_TIME = 10000;

    /*
     * Consumes message from Active MQ for every 50 seconds.
     */

    @Scheduled(cron = "0/50 * * * * ?")
    public void runConsumer() throws JsonProcessingException {
        try {
            log.info("Initializing Message Broker A[ActiveMQ] DHIS2Consumer Connection.");
            log.info("Creating Message Broker A[ActiveMQ] DHIS2Consumer ConnectionFactory.");
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connectionFactory.setTrustedPackages(Arrays.asList("com.lg.shamessagebrokerappA.activemq.consumer",
                    "com.lg.shamessagebrokerappA.activemq.controller", "com.lg.shamessagebrokerappA.activemq.producer",
                    "com.lg.shamessagebrokerappA.activemq.service",
                    "com.lg.shamessagebrokerappA.activemq.messageconfig"));
            log.info("Creating Message Broker A[ActiveMQ] Consumer Connection.");
            connection = connectionFactory.createConnection(uname, password);
            connection.start();
            log.info("Message Broker A[ActiveMQ] DHIS2Consumer Connection started.");
            log.info("Creating Message Broker A[ActiveMQ] DHIS2Consumer Session.");
            receiveSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiveConsumer = receiveSession.createConsumer(receiveSession.createQueue(targetDHIS2Queue));
            while (true) {
                log.info("Querying Queue[{}] to receive message.", targetDHIS2Queue);
                Message msg = receiveConsumer.receive(RECEIVING_TIME);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    String decryptedMessage = DecryptThePayload.decrypt(tm.getText());
                    log.info("Received message from the queue[{}] - {}", targetDHIS2Queue, decryptedMessage);
                } else {
                    log.info("Timeout on receive, bailing.");
                    log.info(
                            "Queue[{}] is empty. Hence, closing the Message Broker A[ActiveMQ] DHIS2Consumer Connection.",
                            targetDHIS2Queue);
                    break;
                }
            }
        } catch (JMSException e) {
            log.error("Message Broker A[ActiveMQ] DHIS2Consumer is not up.{}", e.getMessage());
        } finally {
            log.info("Cleaning up the Message Broker A[ActiveMQ] DHIS2Consumer Connection.");
            try {
                receiveConsumer.close();
                receiveSession.close();
                connection.stop();
                connection.close();
                log.info("Message Broker A[ActiveMQ] DHIS2Consumer Connection has been closed.");
            } catch (Exception e) {
                log.error("Message Broker A[ActiveMQ] DHIS2Consumer connection is not created. {}", e.getMessage());
            }
        }
    }
}