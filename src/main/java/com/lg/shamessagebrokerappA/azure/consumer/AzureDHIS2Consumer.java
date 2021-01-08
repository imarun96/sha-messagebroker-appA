package com.lg.shamessagebrokerappA.azure.consumer;

import java.io.IOException;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.hc.core5.http.ParseException;
import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import lombok.extern.slf4j.Slf4j;

/*
 * Consumes DHIS2 message from ActiveMQ
 */

@Component
@Profile("azure")
@Slf4j
public class AzureDHIS2Consumer {

    @Value("${azure.targetDHIS2Queue}")
    String targetDHIS2Queue;

    @Value("${spring.jms.servicebus.connection-string}")
    String brokerURL;

    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;
    private static final Integer RECEIVING_TIME = 10000;

    /*
     * Consumes message from Azure Service Bus for every 50 seconds.
     */

    @Scheduled(cron = "0/50 * * * * ?")
    public void runConsumer() throws JMSException, NamingException, IOException, ParseException {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            log.info("Initializing Message Broker A[Azure] DHIS2Consumer connection.");
            ConnectionStringBuilder csb = new ConnectionStringBuilder(brokerURL);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
            env.put("connectionfactory.ServiceBusConnectionFactory",
                    "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true");
            log.info("Creating Message Broker A[Azure] DHIS2Consumer Context.");
            Context context = new InitialContext(env);
            log.info("Creating Message Broker A[Azure] DHIS2Consumer ConnectionFactory.");
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ServiceBusConnectionFactory");
            log.info("Creating Message Broker A[Azure] DHIS2Consumer Connection.");
            connection = connectionFactory.createConnection();
            connection.start();
            log.info("Message Broker A[Azure] DHIS2Consumer Connection started.");
            log.info("Creating Message Broker A[Azure] DHIS2Consumer Session.");
            receiveSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiveConsumer = receiveSession.createConsumer(QueueImpl.createQueue(targetDHIS2Queue));
            while (true) {
                log.info("Querying Queue[{}] to receive message.", targetDHIS2Queue);
                Message msg = receiveConsumer.receive(RECEIVING_TIME);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    String decryptedMessage = DecryptThePayload.decrypt(tm.getText());
                    log.info("Received message from the queue[{}] - {}", targetDHIS2Queue, decryptedMessage);
                } else {
                    log.info("Timeout on receive, bailing.");
                    log.info("Queue[{}] is empty. Hence, closing the Message Broker A[Azure] DHIS2Consumer connection.",
                            targetDHIS2Queue);
                    break;
                }
            }
        } catch (JMSException e) {
            log.error("Message Broker A[Azure] DHIS2Consumer is not up. Please check the producer of the other end. {}",
                    e.getMessage());
        } finally {
            log.info("Cleaning up the Message Broker A[Azure] DHIS2Consumer Connection.");
            try {
                receiveConsumer.close();
                receiveSession.close();
                connection.stop();
                connection.close();
            } catch (Exception e) {
                log.error("Message Broker A[Azure] DHIS2Consumer connection is not created. {}", e.getMessage());
            }
        }
    }
}