package com.lg.shamessagebrokerappA.couchdb;

import org.apache.commons.lang3.StringUtils;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("activemq")
public class CouchdbScheduler {

    private CouchDbInstance couchDbInstance;

    protected final RestTemplate restTemplate;

    private Environment environment;

    @Value("${couchdb.database}")
    private String db;

    @Autowired
    public CouchdbScheduler(CouchDbInstance couchDbInstance, Environment environment) {
        this.couchDbInstance = couchDbInstance;
        this.restTemplate = new RestTemplate(couchDbInstance.getConnection());
        this.environment = environment;
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public String eventNotifier() {
        log.info("CouchDB Scheduler has been started.");
        String responseText = StringUtils.EMPTY;
        if (couchDbInstance.checkIfDbExists(db)) {
            log.info("Created CouchDB connection.");
            String path = "/" + db + "/messages-en?";
            HttpResponse response = restTemplate.get(path);
            log.info("Path : {}", path);
            if (response.isSuccessful()) {
                log.info("Fetched a record from Couch Database");
                if (environment.getActiveProfiles()[0].equals("activemq")) {
                    log.info("The Active Profile is - activemq");
                }
                if (environment.getActiveProfiles()[0].equals("sqs")) {
                    log.info("The Active Profile is - sqs");
                }
                if (environment.getActiveProfiles()[0].equals("azure")) {
                    log.info("The Active Profile is - azure");
                }
            } else {
                responseText = "No new record has been updated in the CouchDB";
            }
        } else {
            log.error("Connection with CouchDB Database is unsuccessful.");
        }
        return responseText;
    }
}