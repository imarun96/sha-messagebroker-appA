package com.lg.shamessagebrokerappA.azure.producer;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Profile("azure")
@Service
@Slf4j
public class AzureProducerSender extends AzureProducerSession<javax.jms.QueueSession> {
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
            producer = session.createProducer(QueueImpl.createQueue(queueName));
            producer.send(session.createTextMessage(object));
            return Boolean.TRUE;
        } catch (JMSException e) {
            log.error("Problem in publishing the Payload of Message Broker A[Azure] to Message Broker B[Azure] {}",
                    e.getMessage());
            return Boolean.FALSE;
        } finally {
            try {
                producer.close();
            } catch (JMSException e) {
                log.error("Problem in closing the Message Producer Connection. {}", e.getMessage());
            }
        }
    }
}