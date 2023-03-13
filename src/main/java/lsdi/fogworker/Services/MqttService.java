package lsdi.fogworker.Services;

import org.eclipse.paho.client.mqttv3.*;

public class MqttService {
    private static MqttService instance;

    private MqttConnectOptions options;

    private MqttClient client;

    private static final String MQTT_BROKER_URL = "tcp://localhost:1883";

    private static final String MQTT_CLIENT_ID = "fogworker";

    private MqttService() {
        options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30000);
        options.setKeepAliveInterval(30);

        try {
            client = new MqttClient(MQTT_BROKER_URL, MQTT_CLIENT_ID);
            client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static MqttService getInstance() {
        if (instance == null) instance = new MqttService();
        return instance;
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, payload, 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, IMqttMessageListener callback) {
        try {
            client.subscribe(topic, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMqttClientId() {
        return MQTT_CLIENT_ID;
    }

    public String getMqttBrokerUrl() {
        return MQTT_BROKER_URL;
    }
}
