package com.lg.shamessagebrokerappA.azure.producer;

import java.util.TimerTask;

import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Profile("azure")
@Slf4j
public class AzureProducerRefresh extends TimerTask {

    private AzureProducerService mqService;

    public AzureProducerRefresh(AzureProducerService mqService) {
        this.mqService = mqService;
    }

    private static final Integer MAXTRY = 5;
    private static Integer countValue = 0;

    /*
     * Checks the Azure Service Bus connection for a specific time interval. Also sends
     * message to the support team if the connection is not up.
     */

    @Override
    public void run() {
        while (!mqService.createSession().equals(Boolean.TRUE) && countValue < MAXTRY) {
            countValue++;
        }
        countValue = 0;
        if (!mqService.getIsAlive().equals(Boolean.TRUE)) {
            log.info("Message Broker A[Azure] is not up. Sending mail to support team.");
        } else {
            log.info("Message Broker A[Azure] Producer Connection working properly.");
        }
    }
}