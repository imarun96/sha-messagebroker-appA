package com.lg.shamessagebrokerappA.sqs.consumer;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile("sqs")
@Slf4j
public class SqsDHIS2Consumer {

    @Value("${aws.targetDHIS2Queue}")
    String targetDHIS2Queue;

    @Value("${aws.accessKey}")
    String accessKey;

    @Value("${aws.secretKey}")
    String secretKey;

    @Value("${aws.region}")
    String region;

    private static final Integer RECEIVING_TIME = 10000;
    private SQSConnection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;

    /*
     * Consumes message from MQ for every 50 seconds.
     */

    @Scheduled(cron = "0/50 * * * * ?")
    public void runConsumer() throws JMSException, IOException, ParseException {
        try {
            log.info("Initializing Message Broker A[SQS] Consumer Connection.");
            log.info("Creating Message Broker A[SQS] Consumer ConnectionFactory.");
            SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(),
                    AmazonSQSClientBuilder.standard().withRegion(Regions.fromName(region)).withCredentials(
                            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))));
            log.info("Creating Message Broker A[SQS] Consumer Connection.");
            connection = connectionFactory.createConnection();
            connection.start();
            log.info("Message Broker A[SQS] Consumer Connection started.");
            log.info("Creating Message Broker A[SQS] Consumer Session.");
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
                    log.info("Queue[{}] is empty. Hence, closing the Message Broker A[SQS] Connection.",
                            targetDHIS2Queue);
                    break;
                }
            }
        } catch (JMSException e) {
            log.error("Message Broker A[SQS] Consumer is not up. Please check the producer of the other end. {}",
                    e.getMessage());
        } finally {
            log.info("Cleaning up the Message Broker A[SQS] Consumer Connection.");
            try {
                receiveConsumer.close();
                receiveSession.close();
                connection.stop();
                connection.close();
                log.info("Message Broker A[SQS] Consumer Connection has been closed.");
            } catch (Exception e) {
                log.error("Message Broker A[SQS] connection is not created. {}", e.getMessage());
            }
        }
    }
}