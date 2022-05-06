package com.templars_server.util.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MBMqttClient {

    private static final Logger LOG = LoggerFactory.getLogger(MBMqttClient.class);

    private Unmarshaller unmarshaller;
    private final Map<Class<?>, MBEventCallback<?>> callbackMap;

    public MBMqttClient() {
        callbackMap = new HashMap<>();
    }

    public void connect(String uri, String topic) throws MqttException {
        LOG.info("Initializing unmarshaller");
        try {
            JAXBContext context = JAXBContext.newInstance("generated");
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to initialize unmarshaller", e);
        }

        LOG.info("Connecting to mqtt " + uri);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        MqttClient client = new MqttClient(
                uri,
                UUID.randomUUID().toString(),
                new MemoryPersistence()
        );
        client.connect(options);

        LOG.info("Subscribing to topic " + topic);
        client.subscribe(topic, this::messageArrived);
    }


    public <T> void putEventListener(MBEventListener<T> listener, Class<T> cl) {
        LOG.info("Registered " + cl.getSimpleName());
        callbackMap.put(cl, new MBEventCallback<>(listener));
    }

    private void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        Object event = unmarshaller.unmarshal(new StringReader(payload));

        LOG.info("Received " + event.getClass().getSimpleName());
        raise(event);
    }

    private void raise(Object event) {
        if (callbackMap.containsKey(event.getClass())) {
            callbackMap.get(event.getClass()).raiseEvent(event);
        }
    }

}
