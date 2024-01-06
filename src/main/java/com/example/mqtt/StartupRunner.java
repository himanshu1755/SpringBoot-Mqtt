/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.mqtt;

import com.example.mqtt.connection.MqttClient;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Log4j2
@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private MqttClient mqttClient;

    public void run(String... args) throws Exception {


        mqttClient.init();
    }
}
