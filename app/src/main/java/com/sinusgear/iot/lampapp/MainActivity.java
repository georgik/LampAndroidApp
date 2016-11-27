package com.sinusgear.iot.lampapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    protected boolean isRelayEnabled = false;

    final String serverUri = "tcp://iot.sinusgear.com:1883";
    final String clientId = "AndroidClient";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void changeWifiStatus(boolean status) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(status);
    }


    private void sendRequestToDevice(String value) {
        // Try to use MQTT for request delivery. Possible fallback to direct communication
        if (mqttService != null) {
            mqttService.sendCommand(value);
        } else {
            Log.d(TAG, "Unable to send request. MQTT is not accessible");
        }
    }

    private void setLightIcon(String value) {
        if (lightBulbButton == null) {
            return;
        }

        if (value.equals("on")) {
            lightBulbButton.setBackgroundResource(R.drawable.lightbulb);
            lightBulbButton.setImageResource(R.drawable.lightbulb);
            isRelayEnabled = true;
        } else {
            lightBulbButton.setBackgroundResource(R.drawable.lightbulbdark);
            lightBulbButton.setImageResource(R.drawable.lightbulb);
            isRelayEnabled = false;
        }
        statusText.setText(value);
    }

    private ImageButton lightBulbButton;
    private TextView statusText;
    LampBroadcastReceiver lampBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);
        lightBulbButton = (ImageButton) findViewById(R.id.lightBulbButton);
        lightBulbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRelayEnabled = !isRelayEnabled;
                String value = "off";

                if (isRelayEnabled) {
                    value = "on";
                }
                sendRequestToDevice(value);
            }
        });
    }


    LampMqttService.LocalBinder mBinder;
    LampMqttService mqttService;

    protected void onResume() {
        super.onResume();

        // We need internet connection
        if (!isNetworkAvailable()) {
            changeWifiStatus(true);
        }

        IntentFilter intentFilter = new IntentFilter(LampMqttService.MQTT_RECEIVED_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        lampBroadcastReceiver = new LampBroadcastReceiver();
        registerReceiver(lampBroadcastReceiver, intentFilter);
        Intent serviceIntent = new Intent();
        serviceIntent.setClassName(getApplicationContext(), LampMqttService.MQTT_SERVICE_NAME);
        serviceIntent.setPackage("com.sinusgear.iot.lampapp");

        bindService(serviceIntent,
                new ServiceConnection() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onServiceConnected(ComponentName className, final IBinder service)
                    {
                        Log.d(TAG, "Service connected");
                        mBinder = (LampMqttService.LocalBinder) service;
                        mqttService = mBinder.getServerInstance();
                        mqttService.initializeConnection(serverUri, clientId);
                    }
                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                },
                Context.BIND_AUTO_CREATE);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(lampBroadcastReceiver);
    }

    public class LampBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("value");

            setLightIcon(value);
        }
    }

}
