package com.lg.shamessagebrokerappA.sqs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;
import com.lg.shamessagebrokerappA.sqs.service.SqsPublisherService;

/*
 * Publishes message to SQS
 */

@Profile("sqs")
@RestController
public class SqsController {

    private SqsPublisherService service;

    @Autowired
    public SqsController(SqsPublisherService service) {
        this.service = service;
    }

    /*
     * Publishes OpenMRS message to Amazon SQS.
     * 
     * @param message The desired message that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/sqs/openmrs/publish")
    public String sendMessageToQueue(@RequestBody(required = true) OpenMRSObjectDto message) {
        return service.publish(message);
    }

    /*
     * Publishes DHIS2 message to Amazon SQS.
     * 
     * @param message The desired message that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/sqs/dhis2/publish")
    public String sendMessageToQueue(@RequestBody(required = true) DHIS2ObjectDto message) {
        return service.publish(message);
    }
}