package com.lg.shamessagebrokerappA.activemq.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lg.shamessagebrokerappA.activemq.producer.ProducerMQService;
import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;
import com.lg.shamessagebrokerappA.common.encryption.EncryptThePayload;

@Profile("activemq")
@Service
public class ActiveMQPublisherServiceImpl implements ActiveMQPublisherService {

    private static final Logger log = LoggerFactory.getLogger(ActiveMQPublisherServiceImpl.class);
    public static final String DHIS2_INSTANCE = "DHIS2";
    public static final String OPENMRS_INSTANCE = "OPENMRS";

    private ProducerMQService service;

    @Autowired
    public ActiveMQPublisherServiceImpl(ProducerMQService service) {
        this.service = service;
    }

    /*
     * Publishes OpenMRS message to Active MQ queue.
     * 
     * @param object The message from the request
     * 
     * @return The response as a String
     */

    @Override
    public String publish(OpenMRSObjectDto object) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = StringUtils.EMPTY;
        try {
            jsonString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Problem in Converting Object into JSON. {}", e.getMessage());
        }
        String encryptedMessage = EncryptThePayload.encrypt(jsonString);
        if (Boolean.TRUE.equals(service.sendMessage(encryptedMessage, OPENMRS_INSTANCE))) {
            return "Message published to the queue.";
        } else {
            return "Some error has occured. Reported to support team. Try again later.";
        }
    }

    /*
     * Publishes DHIS2 message to Active MQ queue.
     * 
     * @param object The message from the request
     * 
     * @return The response as a String
     */

    @Override
    public String publish(DHIS2ObjectDto object) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = StringUtils.EMPTY;
        try {
            jsonString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Problem in Converting Object into JSON {}", e.getMessage());
        }
        String encryptedMessage = EncryptThePayload.encrypt(jsonString);
        if (Boolean.TRUE.equals(service.sendMessage(encryptedMessage, DHIS2_INSTANCE))) {
            return "Message published to the queue.";
        } else {
            return "Some error has occured. Reported to support team. Try again later.";
        }
    }
}