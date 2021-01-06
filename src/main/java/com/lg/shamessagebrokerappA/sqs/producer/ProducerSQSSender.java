package com.lg.shamessagebrokerappA.sqs.producer;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("sqs")
@Service
public class ProducerSQSSender extends ProducerSQSSession<javax.jms.QueueSession> {
    private static final Logger log = LoggerFactory.getLogger(ProducerSQSSender.class);
    private MessageProducer producer;
    /*
     * Publish the message to the queue.
     * 
     * @param object The message from the request
     * 
     * @param queueName The destination queue name
     */

    public Boolean sendMessage(String object, String queueName) {
        try {
            producer = session.createProducer(session.createQueue(queueName));
            producer.send(session.createTextMessage(object));
            return Boolean.TRUE;
        } catch (JMSException e) {
            log.error("Problem in publishing the Payload of Message Broker A[SQS] to Message Broker B[SQS]. {}",
                    e.getMessage());
            return Boolean.FALSE;
        } finally {
            try {
                producer.close();
            } catch (Exception e) {
                log.error("Problem in closing the QueueSender. {}", e.getMessage());
            }
        }
    }
}