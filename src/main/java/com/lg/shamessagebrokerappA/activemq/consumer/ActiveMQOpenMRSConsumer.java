package com.lg.shamessagebrokerappA.activemq.consumer;

import java.io.IOException;
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

import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;

import lombok.extern.slf4j.Slf4j;

/*
 * Consumes OpenMRS message from ActiveMQ
 */

@Component
@Profile("activemq")
@Slf4j
public class ActiveMQOpenMRSConsumer {

    @Value("${activeMQ.brokerURL}")
    String brokerURL;

    @Value("${activeMQ.uname}")
    String uname;

    @Value("${activeMQ.password}")
    String password;

    @Value("${activeMQ.targetOpenMRSQueue}")
    String targetOpenMRSQueue;

    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;
    private static final Integer RECEIVING_TIME = 10000;
    /*
     * Consumes message from Active MQ for every 50 seconds.
     */

    @Scheduled(cron = "0/50 * * * * ?")
    public void runConsumer() throws JMSException, IOException {
        try {
            log.info("Initializing Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer ConnectionFactory.");
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connectionFactory.setTrustedPackages(Arrays.asList("com.lg.shamessagebrokerappA.activemq.consumer",
                    "com.lg.shamessagebrokerappA.activemq.controller", "com.lg.shamessagebrokerappA.activemq.producer",
                    "com.lg.shamessagebrokerappA.activemq.service",
                    "com.lg.shamessagebrokerappA.activemq.messageconfig"));
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            connection = connectionFactory.createConnection(uname, password);
            connection.start();
            log.info("Message Broker A[ActiveMQ] OpenMRSConsumer Connection started.");
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer Session.");
            receiveSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiveConsumer = receiveSession.createConsumer(receiveSession.createQueue(targetOpenMRSQueue));
            while (true) {
                log.info("Querying Queue[{}] to receive message.", targetOpenMRSQueue);
                Message msg = receiveConsumer.receive(RECEIVING_TIME);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    String decryptedMessage = DecryptThePayload.decrypt(tm.getText());
                    log.info("Received message from the queue[{}] - {}", targetOpenMRSQueue, decryptedMessage);
                } else {
                    log.info("Timeout on receive, bailing.");
                    log.info(
                            "Queue[{}] is empty. Hence, closing the Message Broker A[ActiveMQ] OpenMRSConsumer Connection.",
                            targetOpenMRSQueue);
                    break;
                }
            }
        } catch (JMSException e) {
            log.error(
                    "Message Broker A[ActiveMQ] OpenMRSConsumer is not up. Please check the producer of the other end. {}",
                    e.getMessage());
        } finally {
            log.info("Cleaning up the Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            try {
                receiveConsumer.close();
                receiveSession.close();
                connection.stop();
                connection.close();
                log.info("Message Broker A[ActiveMQ] OpenMRSConsumer Connection has been closed.");
            } catch (Exception e) {
                log.error("Message Broker A[ActiveMQ] OpenMRSConsumer connection is not created. {}", e.getMessage());
            }
        }
    }
}