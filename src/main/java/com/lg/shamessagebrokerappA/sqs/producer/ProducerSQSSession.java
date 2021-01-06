package com.lg.shamessagebrokerappA.sqs.producer;

import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Profile("sqs")
public class ProducerSQSSession<S extends javax.jms.Session> {

    protected S session = null;
    private Boolean isAlive = Boolean.TRUE;
    private static final Logger log = LoggerFactory.getLogger(ProducerSQSSession.class);
    /*
     * Creates a session in ActiveMQ.
     * 
     * @return boolean The connection status of ActiveMQ
     */

    @SuppressWarnings("unchecked")
    public Boolean connect(String accessKey, String secretKey, String region) {
        log.info("Initializing Message Broker B[SQS] Producer Connection.");
        log.info("Creating Message Broker B[SQS] Producer ConnectionFactory.");
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard().withRegion(Regions.fromName(region)).withCredentials(
                        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))));
        try {
            log.info("Creating Message Broker A[SQS] Producer Connection.");
            SQSConnection conn = connectionFactory.createConnection();
            log.info("Starting Message Broker A[SQS] Producer Connection.");
            conn.start();
            log.info("Started Message Broker A[SQS] Producer Connection.");
            log.info("Creating Message Broker A[SQS] Producer Session.");
            session = (S) conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            isAlive = Boolean.TRUE;
        } catch (JMSException e) {
            log.error("Problem in creating connection with Message Broker A[SQS] Queue {}", e.getMessage());
            isAlive = Boolean.FALSE;
            return false;
        }
        return true;
    }

    public Boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean isAlive) {
        this.isAlive = isAlive;
    }
}