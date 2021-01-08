package com.lg.shamessagebrokerappA.common.config;

import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * To provide the configuration to connect the couch database.
 * </p>
 */
@Configuration
@Slf4j
public class CouchDatabaseConfig {

    @Value("${couchdb.url}")
    private String url;

    @Value("${couchdb.username}")
    private String userName;

    @Value("${couchdb.password}")
    private String password;

    @Value("${couchdb.database}")
    private String database;

    @Bean
    public CouchDbConnector couchDbConnector() throws MalformedURLException {
        CouchDbConnector connector = dbInstance().createConnector(database, true);
        log.debug("Couch DB connetion created");
        return connector;
    }

    /**
     * <p>
     * Create the instance for the couch database based on given credentials.
     * </p>
     * 
     * @return Couch DB Instance
     * @throws MalformedURLException
     */
    @Bean
    public CouchDbInstance dbInstance() throws MalformedURLException {
        HttpClient httpClient = new StdHttpClient.Builder().url(url).username(userName).password(password).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        log.debug("Couch DB instance created");
        return dbInstance;
    }
}