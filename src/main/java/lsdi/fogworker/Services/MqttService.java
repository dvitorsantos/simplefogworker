package lsdi.fogworker.Services;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttService {
    private static MqttService instance;

    private MqttConnectOptions options;

    private MqttClient client;

    @Value("${mosquitto.url}")
    private String mosquittoUrl;

    @Value("${mosquitto.clientid}")
    private String clientUuid;


    private MqttService() {
        options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30000);
        options.setKeepAliveInterval(30);

        try {
            client = new MqttClient("tcp://localhost:1883", "fogworker");
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
}
