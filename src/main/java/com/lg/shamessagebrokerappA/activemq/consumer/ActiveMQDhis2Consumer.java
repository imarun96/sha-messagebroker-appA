package com.lg.shamessagebrokerappA.activemq.consumer;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.hc.core5.http.ParseException;
import org.hisp.dhis.Dhis2;
import org.hisp.dhis.Dhis2Config;
import org.hisp.dhis.model.datavalueset.DataValue;
import org.hisp.dhis.model.datavalueset.DataValueSet;
import org.hisp.dhis.model.datavalueset.DataValueSetImportOptions;
import org.hisp.dhis.response.datavalueset.DataValueSetResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;

/*
 * Consumes DHIS2 message from ActiveMQ
 */

@Component
@Profile("activemq")
public class ActiveMQDhis2Consumer {

    @Value("${activeMQ.brokerURL}")
    String brokerURL;

    @Value("${activeMQ.uname}")
    String uname;

    @Value("${activeMQ.password}")
    String password;

    @Value("${activeMQ.targetDHIS2Queue}")
    String targetDHIS2Queue;

    @Value("${dhis2.uname}")
    String dhis2Uname;

    @Value("${dhis2.password}")
    String dhis2Password;

    @Value("${dhis2.url}")
    String dhis2InstanceURL;

    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;
    private static final Integer RECEIVING_TIME = 10000;
    private static List<DHIS2ObjectDto> deadLetterObjects = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ActiveMQDhis2Consumer.class);

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
                    ObjectMapper mapper = new ObjectMapper();
                    DHIS2ObjectDto dhis2Object = mapper.readValue(decryptedMessage, DHIS2ObjectDto.class);
                    createOrgUnitInDhis2(dhis2Object);
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

    /*
     * Creates a new Data Entry in DHIS2.
     * 
     * @param dhis2Object The consumed object from ActiveMQ
     * 
     */

    private void createOrgUnitInDhis2(DHIS2ObjectDto dhis2Object) {
        log.info("Creating DHIS2 Configuration.");
        Dhis2Config config = new Dhis2Config(dhis2InstanceURL, dhis2Uname + "asd", dhis2Password);
        Dhis2 dhis2 = new Dhis2(config);
        log.info("DHIS2 Configuration created.");
        DataValue dataValue1 = new DataValue();
        DataValue dataValue2 = new DataValue();
        DataValue dataValue3 = new DataValue();
        DataValue dataValue4 = new DataValue();
        DataValue dataValue5 = new DataValue();
        DataValue dataValue6 = new DataValue();
        DataValue dataValue7 = new DataValue();
        DataValue dataValue8 = new DataValue();
        dataValue1.setDataElement("VaXeNDMVsHu");
        dataValue1.setValue(dhis2Object.getBloodPressure());
        dataValue2.setDataElement("P6DnfXmdCVW");
        dataValue2.setValue(dhis2Object.getHeight());
        dataValue3.setDataElement("BzmyVsTyJW7");
        dataValue3.setValue(dhis2Object.getPatientAddress());
        dataValue4.setDataElement("Y76hjzekKWl");
        dataValue4.setValue(dhis2Object.getPatientName());
        dataValue5.setDataElement("WkAGadEJxgS");
        dataValue5.setValue(dhis2Object.getPulse());
        dataValue6.setDataElement("S1hMfShJwKZ");
        dataValue6.setValue(dhis2Object.getRespiratoryRate());
        dataValue7.setDataElement("GJ17wPl5vi0");
        dataValue7.setValue(dhis2Object.getTemperature());
        dataValue8.setDataElement("HUoqJwVWBzv");
        dataValue8.setValue(dhis2Object.getWeight());
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataSet(dhis2Object.getDataSet());
        dataValueSet.setCompleteDate(Date.valueOf(LocalDate.now()).toString());
        dataValueSet.setPeriod(dhis2Object.getPeriod());
        dataValueSet.setOrgUnit(dhis2Object.getOrgUnit());
        dataValueSet.addDataValue(dataValue1);
        dataValueSet.addDataValue(dataValue2);
        dataValueSet.addDataValue(dataValue3);
        dataValueSet.addDataValue(dataValue4);
        dataValueSet.addDataValue(dataValue5);
        dataValueSet.addDataValue(dataValue6);
        dataValueSet.addDataValue(dataValue7);
        dataValueSet.addDataValue(dataValue8);
        DataValueSetImportOptions options = DataValueSetImportOptions.instance();
        try {
            DataValueSetResponseMessage response = dhis2.saveDataValueSet(dataValueSet, options);
            if (response.getStatus().toString().equals("SUCCESS")) {
                log.info("Data Entry for the Period [{}] has been captured successfully for the Org Unit [{}]",
                        dhis2Object.getPeriod(), dhis2Object.getOrgUnit());
            } else {
                deadLetterObjects.add(dhis2Object);
                log.error("Some error has been occured in capturing Data Entry.");
            }
        } catch (IOException | ParseException e) {
            deadLetterObjects.add(dhis2Object);
            log.error("Some error has been occured while saving Data Entry into DHIS2, {}", e.getMessage());
        }
    }
}