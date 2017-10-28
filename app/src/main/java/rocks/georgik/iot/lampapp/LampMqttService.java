package rocks.georgik.iot.lampapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LampMqttService extends Service implements MqttCallbackExtended, IMqttActionListener, IMqttMessageListener {

    private static final String TAG = LampMqttService.class.getCanonicalName();

    public static final String MQTT_SERVICE_NAME = "rocks.georgik.iot.lampapp.LampMqttService";
    final static String MQTT_RECEIVED_ACTION = "rocks.georgik.iot.lampapp.LampMqttService.MQTT_RECEIVED";
    final static String MQTT_SEND_ACTION = "rocks.georgik.iot.lampapp.LampMqttService.MQTT_SEND";

    final String serverUri = "tcp://iot.georgik.rocks:1883";

    final String subscriptionTopic = "/home/+/+";
    final String publishTopic = "/home/bedroom/command";

    MqttAndroidClient mqttAndroidClient;

    IBinder mBinder = new LocalBinder();

    public LampMqttService()
    {
        super();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            Log.d(TAG, "Reconnected to : " + serverURI);
            // Because Clean Session is true, we need to re-subscribe
        } else {
            Log.d(TAG, "Connected to: " + serverURI);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "The Connection was lost.");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "Subscribed!");
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.d(TAG, "Failed to subscribe");
    }


    public void sendBroadcast(String topic, String value) {
        Intent intent = new Intent();
        intent.setAction(MQTT_RECEIVED_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("topic", topic);
        intent.putExtra("value", value);
        sendBroadcast(intent);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String value = new String(message.getPayload());
        Log.d(TAG, "Message: " + topic + " : " + value);

        // Do not process commands
        if (topic.contains("command")) {
            return;
        }

        if (topic.endsWith("relay")) {
            if ((value.equals("on")) || (value.equals("off"))) {

                sendBroadcast("relay", value);
            }
            return;
        }

        if (topic.endsWith("temperature")) {
            sendBroadcast("temperature", value);
        }

        if (topic.endsWith("humidity")) {
            sendBroadcast("humidity", value);
        }

        if (topic.endsWith("heat")) {
            sendBroadcast("heat", value);
        }

        if (topic.endsWith("pir")) {
            sendBroadcast("pir", value);
        }

        if (topic.endsWith("air")) {
            sendBroadcast("air", value);
        }
    }

    public void sendCommand(String value) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = value.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            mqttAndroidClient.publish(publishTopic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, this);
        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private LampServiceBroadcastReceiver lampServiceBroadcastReceiver;

    private void registerLocalReceivers() {
        lampServiceBroadcastReceiver = new LampServiceBroadcastReceiver();
        registerReceiver(lampServiceBroadcastReceiver, new IntentFilter(
                    MQTT_SEND_ACTION));
    }


    private void unregisterLocalReceivers() {
        unregisterReceiver(lampServiceBroadcastReceiver);
    }

    public class LocalBinder extends Binder {
        public LampMqttService getServerInstance() {
            return LampMqttService.this;
        }
    }

    public class LampServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("value");
            sendCommand(value);
        }
    }

    @Override
    public void onDestroy() {
        unregisterLocalReceivers();
        super.onDestroy();
    }

    private String getClientId() {
        String deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String deviceString;
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(deviceId.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            deviceString = hexString.toString().substring(0,6);

        } catch (NoSuchAlgorithmException e) {
            deviceString = "Unknown";
        }
        return "Android-" + android.os.Build.MODEL + "-" + deviceString;
    }

    String extraCommand = null;

    public void initializeConnection() {
        if (mqttAndroidClient != null) {
            return;
        }
        final String localCommand = extraCommand;
        registerLocalReceivers();
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        mqttAndroidClient = new MqttAndroidClient(this, serverUri, getClientId());
        mqttAndroidClient.setCallback(this);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    if (localCommand != null) {
                        if (localCommand.equals("toggle")) {
                            sendCommand("toggle");
                        }
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect to MQTT");
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        mqttAndroidClient.unregisterResources();
    }

    public int onStartCommand(final Intent intent, int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

}
