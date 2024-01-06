/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.mqtt.connection;


import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;


@Log4j2
@Component
public class MqttClient implements MqttCallbackExtended {

    @Autowired
    private TaskExecutor taskExecutor;



    @Value("${mqtt.serverUrl}")
    private String mqttServerAddress;//tcp://192.168.1.46:1883";

    @Value("${mqtt.automaticReconnect}")
    private Boolean automaticReconnect;

    @Value("${mqtt.cleanSession}")
    private Boolean cleanSession;

    @Value("${mqtt.connectionTimeout}")
    private Integer connectionTimeout;

    private IMqttAsyncClient instance;

    private MqttClient() {
    }

    public void init() {
        try {
            log.debug("mqttServerAddress : {}", mqttServerAddress);
            log.debug("automaticReconnect : {}", automaticReconnect);
            log.debug("cleanSession : {}", cleanSession);
            log.debug("connectionTimeout : {}", connectionTimeout);
            if (this.instance == null) {
                log.debug("initializing mqtt instance");
                this.instance = new MqttAsyncClient(mqttServerAddress, "mqtt-client" + MqttAsyncClient.generateClientId(), new MemoryPersistence());
                //set this as a callback so that we get all mqtt event within same class (e.g. connectComplete, messageArrived e.t.c.)
                this.instance.setCallback(this);
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(automaticReconnect);
            options.setCleanSession(cleanSession);
            options.setConnectionTimeout(connectionTimeout);

            if (!this.instance.isConnected()) {
                log.debug("connecting mqtt instance");
                this.instance.connect(options);
            }
        } catch (MqttException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.debug("MQTT client Connected to Admin Broker at serverURI: [{}], reconnect: {}", serverURI, reconnect);
        if (reconnect) {
            if (this.instance != null && this.instance.isConnected()) {
                log.debug("Reconnected MQTT client: id:[{}]", this.instance.getClientId());
                subscribe("himanshu");


            } else {
                log.debug("MQTT Client not connected running at serverURI: [{}]", serverURI);
            }
        } else {
            log.debug("Connected MQTT Client:[{}]", this.instance.getClientId());
            subscribe("himanshu");

        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage payload) throws Exception {
        try {
            if (topic == null || topic.trim().isEmpty()) {
                log.warn("MQTT topic is null or empty. topic:[{}]", topic);
                return;
            }
            if (payload == null || payload.toString().isEmpty()) {
                log.warn("MQTT received Message is null or empty for topic:[{}]", topic);
                return;
            }

            log.debug("topic:({}) ----- Message: {}", topic, payload.toString());

        } catch (Exception e) {
            log.error("Exception occurred. {}", e.getMessage());
            log.catching(Level.FATAL, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            if (iMqttDeliveryToken.getMessage() != null) {
                log.trace("MQTT Message deliveryComplete: ", iMqttDeliveryToken.getMessage().toString());
            } else {
                log.trace("MQTT Message deliveryComplete");
            }
        } catch (MqttException e) {
            log.error("Exception occurred. {}", e.getMessage());
            log.catching(Level.FATAL, e);
        }
    }

    private void subscribe(String topic) {
        this.subscribe(topic, 1);
    }

    public void subscribe(String topic, int sQoS) {
        log.info("MqttClient Id: {} ", this.instance.getClientId());
        try {
            if (this.instance != null && this.instance.isConnected()) {
                this.instance.subscribe(topic, sQoS);
            } else {
                log.warn("MQTT client is not connected. Reconnect it...");
                init();
            }
        } catch (MqttException e) {
            log.error("Exception occurred. {}", e.getMessage());
            log.catching(Level.FATAL, e);
        }
    }

    public void unSubscribe(String topic) {
        log.debug("unSubscribe MqttClient Id: {} ", this.instance.getClientId());
        try {
            if (this.instance.isConnected()) {
                log.debug("Uubscribe : topic {}", topic);
                this.instance.unsubscribe(topic);
            } else {
                log.warn("MQTT client is not connected.");
            }
        } catch (MqttException e) {
            log.catching(Level.FATAL, e);
        }
    }

    public void publish(String topicName, String payload) {
        this.publish(topicName, payload, 0);
    }

    public void publish(String topicName, String payload, boolean retained) {
        this.publish(topicName, payload, 1, retained);
    }

    public void publish(String topicName, String payload, int pQoS) {
        this.publish(topicName, payload, pQoS, false);
    }

    public void publish(String topic, String payload, int pQoS, boolean retain) {
        log.debug("Topic: {}", topic);
        log.debug("Payload: {}", payload);
        log.trace("clientId: {}", instance.getClientId());
        log.trace("PQos: {}", pQoS);
        log.trace("retain: {}", retain);
        try {
            if (this.instance != null && this.instance.isConnected()) {
                this.instance.publish(topic, payload.getBytes(), pQoS, retain);
            } else {
                log.warn("********* ADMIN MQTT NOT CONNECTED ********");
                log.warn("Discarding the Data Data: {}", topic, payload);
                log.warn("Topic Name: {}", topic);
                log.warn("Payload: {}", payload);
            }

        } catch (MqttException exp) {
            log.error("Exception occurred. {}", exp.getMessage());
            log.catching(Level.FATAL, exp);
        }
    }




}
