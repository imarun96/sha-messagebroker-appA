package com.lg.shamessagebrokerappA.activemq.producer;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("activemq")
@Service
public class ProducerMQSender extends ProducerMQSession<javax.jms.QueueSession> {

    private static final Logger log = LoggerFactory.getLogger(ProducerMQSender.class);
    private QueueSender sender;

    /*
     * Publish the message to the queue.
     * 
     * @param object The message from the request
     * 
     * @param queueName The destination queue name
     */

    public Boolean sendMessage(String object, String queueName) {
        try {
            Queue queue = session.createQueue(queueName);
            sender = session.createSender(queue);
            sender.send(session.createTextMessage(object));
            return Boolean.TRUE;
        } catch (JMSException e) {
            log.error(
                    "Problem in publishing the Payload of Message Broker A[ActiveMQ] to Message Broker B[ActiveMQ]. {}",
                    e.getMessage());
            return Boolean.FALSE;
        } finally {
            try {
                sender.close();
            } catch (Exception e) {
                log.error("Problem in closing the QueueSender. {}", e.getMessage());
            }
        }
    }
}