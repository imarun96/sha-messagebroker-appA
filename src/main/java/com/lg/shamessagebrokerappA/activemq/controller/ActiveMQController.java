package com.lg.shamessagebrokerappA.activemq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lg.shamessagebrokerappA.activemq.service.ActiveMQPublisherService;
import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;

/*
 * Publishes message to ActiveMQ
 */

@RestController
@Profile("activemq")
public class ActiveMQController {

    private ActiveMQPublisherService service;

    @Autowired
    public ActiveMQController(ActiveMQPublisherService service) {
        this.service = service;
    }

    /*
     * Publishes DHIS2 message to Active MQ.
     * 
     * @param object The desired object that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/activemq/dhis2/publish")
    public String sendDHIS2MessageToQueue(@RequestBody(required = true) DHIS2ObjectDto object) {
        return service.publish(object);
    }

    /*
     * Publishes OpenMRS message to Active MQ.
     * 
     * @param object The desired object that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/activemq/openmrs/publish")
    public String sendOpenMRSMessageToQueue(@RequestBody(required = true) OpenMRSObjectDto object) {
        return service.publish(object);
    }
}