package com.templars_server.util.mqtt;

import com.templars_server.mb2_log_reader.schema.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MBMqttClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(MBMqttClientTest.class);
    private static final String TEST_TOPIC = "test_topic";
    private static final String TEST_MESSAGE = "<ShutdownGameEvent></ShutdownGameEvent>";
    private static MBMqttClient client;
    private static boolean flag;

    @BeforeEach
    void setup() {
        client = new MBMqttClient();
        try {
            client.connect(null, null);
        } catch (Exception e) {
            LOG.warn("Warning", e);
        }
        client.putEventListener(MBMqttClientTest::onTestObject, ShutdownGameEvent.class);
        flag = false;
    }

    static void onTestObject(ShutdownGameEvent testObject) {
        flag = true;
        assertThat(testObject).isNotNull();
    }

    @Test
    void testMessageArrived_ShutdownGameEvent_ExpectedEvent() {
        MqttMessage testMessage = new MqttMessage();
        testMessage.setPayload(TEST_MESSAGE.getBytes(StandardCharsets.UTF_8));
        client.messageArrived(TEST_TOPIC, testMessage);
        assertThat(flag).isTrue();
    }

}
