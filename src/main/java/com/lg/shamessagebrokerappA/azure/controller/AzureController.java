package com.lg.shamessagebrokerappA.azure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lg.shamessagebrokerappA.azure.service.AzurePublisherService;
import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;

/*
 * Publishes message to ActiveMQ
 */

@RestController
@Profile("azure")
public class AzureController {

    private AzurePublisherService service;

    @Autowired
    public AzureController(AzurePublisherService service) {
        this.service = service;
    }

    /*
     * Publishes OpenMRS message to Azure Service Bus.
     * 
     * @param object The desired object that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/azure/openmrs/publish")
    public String sendOpenMRSMessageToQueue(@RequestBody(required = true) OpenMRSObjectDto object) {
        return service.publish(object);
    }

    /*
     * Publishes DHIS2 message to Azure Service Bus.
     * 
     * @param object The desired object that is required to publish
     * 
     * @return Response as a String message
     */

    @PostMapping("/azure/dhis2/publish")
    public String sendDHIS2MessageToQueue(@RequestBody(required = true) DHIS2ObjectDto object) {
        return service.publish(object);
    }
}