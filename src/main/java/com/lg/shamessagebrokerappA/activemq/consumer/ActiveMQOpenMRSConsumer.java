package com.lg.shamessagebrokerappA.activemq.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.DatatypeConverter;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lg.shamessagebrokerappA.common.dto.OpenMRSObjectDto;
import com.lg.shamessagebrokerappA.common.encryption.DecryptThePayload;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 * Consumes OpenMRS message from ActiveMQ
 */

@Component
@Profile("activemq")
public class ActiveMQOpenMRSConsumer {

    @Value("${activeMQ.brokerURL}")
    String brokerURL;

    @Value("${activeMQ.uname}")
    String uname;

    @Value("${activeMQ.password}")
    String password;

    @Value("${activeMQ.targetOpenMRSQueue}")
    String targetOpenMRSQueue;

    @Value("${openmrs.openMRSUname}")
    String openMRSUname;

    @Value("${openmrs.openMRSPassword}")
    String openMRSPassword;

    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiveConsumer;
    private static final Integer SESSION_LENGTH = 32;
    private static final String AUTHORIZATION_HEADER_VALUE = "Authorization";
    private static Integer numberOfAttempts = 1;
    private static final Integer MAX_ATTEMPTS = 6;
    private static final String COOKIE = "Cookie";
    private static final String BASE_URL = "https://openmrs.livinggoods.net/openmrs/ws/rest/v1";
    private static final String BASIC = "Basic ";
    private static final Integer RECEIVING_TIME = 10000;
    private static List<OpenMRSObjectDto> deadLetterObjects = new ArrayList<>();
    private static boolean alreadyExists = false;
    private static final Logger log = LoggerFactory.getLogger(ActiveMQOpenMRSConsumer.class);

    /*
     * Consumes message from Active MQ for every 50 seconds.
     */

    @Scheduled(cron = "0/50 * * * * ?")
    public void runConsumer() throws JMSException, IOException {
        try {
            log.info("Initializing Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer ConnectionFactory.");
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connectionFactory.setTrustedPackages(Arrays.asList("com.lg.shamessagebrokerappA.activemq.consumer",
                    "com.lg.shamessagebrokerappA.activemq.controller", "com.lg.shamessagebrokerappA.activemq.producer",
                    "com.lg.shamessagebrokerappA.activemq.service",
                    "com.lg.shamessagebrokerappA.activemq.messageconfig"));
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            connection = connectionFactory.createConnection(uname, password);
            connection.start();
            log.info("Message Broker A[ActiveMQ] OpenMRSConsumer Connection started.");
            log.info("Creating Message Broker A[ActiveMQ] OpenMRSConsumer Session.");
            receiveSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiveConsumer = receiveSession.createConsumer(receiveSession.createQueue(targetOpenMRSQueue));
            while (true) {
                log.info("Querying Queue[{}] to receive message.", targetOpenMRSQueue);
                Message msg = receiveConsumer.receive(RECEIVING_TIME);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    String decryptedMessage = DecryptThePayload.decrypt(tm.getText());
                    log.info("Received message from the queue[{}] - {}", targetOpenMRSQueue, decryptedMessage);
                    ObjectMapper mapper = new ObjectMapper();
                    OpenMRSObjectDto openMRSObject = mapper.readValue(decryptedMessage, OpenMRSObjectDto.class);
                    String sessionID = getSessionIdForOpenMRS(openMRSUname, openMRSPassword, openMRSObject);
                    createPatientInOpenMRS(sessionID, openMRSObject, openMRSUname, openMRSPassword);
                } else {
                    log.info("Timeout on receive, bailing.");
                    log.info(
                            "Queue[{}] is empty. Hence, closing the Message Broker A[ActiveMQ] OpenMRSConsumer Connection.",
                            targetOpenMRSQueue);
                    break;
                }
            }
        } catch (JMSException e) {
            log.error(
                    "Message Broker A[ActiveMQ] OpenMRSConsumer is not up. Please check the producer of the other end. {}",
                    e.getMessage());
        } finally {
            log.info("Cleaning up the Message Broker A[ActiveMQ] OpenMRSConsumer Connection.");
            try {
                alreadyExists = false;
                receiveConsumer.close();
                receiveSession.close();
                connection.stop();
                connection.close();
                log.info("Message Broker A[ActiveMQ] OpenMRSConsumer Connection has been closed.");
            } catch (Exception e) {
                log.error("Message Broker A[ActiveMQ] OpenMRSConsumer connection is not created. {}", e.getMessage());
            }
        }
    }

    /*
     * Creates a Patient in OpenMRS.
     * 
     * @param patient The consumed object from Active MQ
     * 
     * @param sessionID Session retrieved from OpenMRS
     * 
     * @param openMRSUname user name for OpenMRS
     * 
     * @param openMRSPassword password for OpenMRS
     * 
     */

    @SuppressWarnings("deprecation")
    private static void createPatientInOpenMRS(String sessionID, OpenMRSObjectDto openMRSObject, String openMRSUname,
            String openMRSPassword) throws IOException {
        String authorizationFormat = openMRSUname + ":" + openMRSPassword;
        String encodingFormat = DatatypeConverter
                .printBase64Binary(authorizationFormat.getBytes(StandardCharsets.UTF_8.name()));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\r\n    \"names\": [\r\n        {\r\n        \"givenName\": \"" + openMRSObject.getGivenName()
                        + "\",\r\n        \"familyName\": \"" + openMRSObject.getFamilyName()
                        + "\"\r\n        }\r\n    ],\r\n    \"gender\": \"" + openMRSObject.getGender()
                        + "\",\r\n    \"birthdate\": \"" + openMRSObject.getBirthdate()
                        + "\",\r\n    \"addresses\": [\r\n        {\r\n        \"address1\": \""
                        + openMRSObject.getAddress1() + "\",\r\n        \"cityVillage\": \""
                        + openMRSObject.getCityVillage() + "\",\r\n        \"country\": \"" + openMRSObject.getCountry()
                        + "\",\r\n        \"postalCode\": \"" + openMRSObject.getPostalCode()
                        + "\"\r\n        }\r\n    ]\r\n}");
        Request request = new Request.Builder().url(BASE_URL + "/person").method(HttpMethod.POST.name(), body)
                .addHeader(AUTHORIZATION_HEADER_VALUE, BASIC + encodingFormat)
                .addHeader("Content-Type", org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .addHeader(COOKIE, "JSESSIONID=" + sessionID).build();
        Response response = client.newCall(request).execute();
        String personUUID = response.body().string();
        log.info("Response message from Create Person EndPoint -  {}", personUUID);
        JSONObject jsonObject = new JSONObject(personUUID);
        String createdPersonUUID = jsonObject.getString("uuid");
        log.info("UUID of newly created Person - {}", createdPersonUUID);
        client = new OkHttpClient().newBuilder().build();
        mediaType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        body = RequestBody.create(mediaType, "{\r\n    \"person\": \"" + createdPersonUUID
                + "\",\r\n    \"identifiers\": [\r\n        {\r\n            \"identifier\": \"1003EY\",\r\n            \"identifierType\": \"05a29f94-c0ed-11e2-94be-8c13b969e334\",\r\n            \"location\": \"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\r\n            \"preferred\": false\r\n        }\r\n    ]\r\n}");
        request = new Request.Builder().url(BASE_URL + "/patient").method(HttpMethod.POST.name(), body)
                .addHeader(AUTHORIZATION_HEADER_VALUE, BASIC + encodingFormat)
                .addHeader("Content-Type", org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .addHeader(COOKIE, "JSESSIONID=" + sessionID).build();
        response = client.newCall(request).execute();
        String patientUUID = response.body().string();
        log.info("Response message from Create Patient EndPoint -  {}", patientUUID);
        jsonObject = new JSONObject(patientUUID);
        try {
            JSONArray createdPatientUUID = jsonObject.getJSONArray("links");
            log.info("Patient has been created and the URI is = {}",
                    new JSONObject(createdPatientUUID.get(0).toString()).getString("uri"));
        } catch (JSONException e) {
            if (!alreadyExists) {
                deadLetterObjects.add(openMRSObject);
                alreadyExists = true;
            }
            log.error("Problem in finding the element from the JSON. {}", e.getMessage());
        }
    }

    /*
     * Retrieves Session from OpenMRS.
     * 
     * @param openMRSUname user name for OpenMRS
     * 
     * @param openMRSPassword password for OpenMRS
     * 
     * @return Response as a String message
     */

    private static String getSessionIdForOpenMRS(String openMRSUname, String openMRSPassword,
            OpenMRSObjectDto openMRSObject) throws IOException {
        try {
            String authorizationFormat = openMRSUname + ":" + openMRSPassword;
            String encodingFormat = DatatypeConverter
                    .printBase64Binary(authorizationFormat.getBytes(StandardCharsets.UTF_8.name()));
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(BASE_URL + "/session").method(HttpMethod.GET.name(), null)
                    .addHeader(AUTHORIZATION_HEADER_VALUE, BASIC + encodingFormat).build();
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            log.info("Response received from OpenMRS - {}", responseString);
            JSONObject jsonObj = new JSONObject(responseString);
            String session = jsonObj.getString("sessionId");
            log.info("SessionId received from OpenMRS - {}", session);
            if (session.length() == SESSION_LENGTH) {
                return session;
            } else {
                if (numberOfAttempts < MAX_ATTEMPTS) {
                    log.info(
                            "Number of attempts to retrieve session from OpenMRS - {}. Still {} attempts remaining to reach max try.",
                            numberOfAttempts, MAX_ATTEMPTS - 1 - numberOfAttempts);
                    numberOfAttempts += 1;
                    getSessionIdForOpenMRS(openMRSUname, openMRSPassword, openMRSObject);
                } else {
                    if (!alreadyExists) {
                        deadLetterObjects.add(openMRSObject);
                        alreadyExists = true;
                    }
                    throw new RuntimeException(
                            "Unable to fetch Session from OpenMRS. Please check the OpenMRS instance.");
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Problem in forming Basic Authorization header {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }
}