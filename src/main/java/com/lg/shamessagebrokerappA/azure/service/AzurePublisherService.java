package com.lg.shamessagebrokerappA.azure.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.lg.shamessagebrokerappA.common.dto.DHIS2ObjectDto;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;

@Profile("azure")
@Service
public interface AzurePublisherService {
    public String publish(DHIS2ObjectDto object);

    public String publish(OpenMRSObjectDto object);
}