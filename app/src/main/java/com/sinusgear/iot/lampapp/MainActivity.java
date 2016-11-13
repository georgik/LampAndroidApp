package com.sinusgear.iot.lampapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    protected boolean isRelayEnabled = false;
    protected boolean lastWifiState = false;

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

    /**
     * Send HTTP request. When Wifi is down it will turn it on and then off.
     * @param state
     */
    private void getHttpRequest(String state) {
        lastWifiState = isNetworkAvailable();
        if (!lastWifiState) {
            changeWifiStatus(true);
        }

        AsyncHttpClient asyncClient = new AsyncHttpClient();
        asyncClient.get("http://192.168.1.50/relay=" + state, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (!lastWifiState) {
                    changeWifiStatus(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (!lastWifiState) {
                    changeWifiStatus(false);
                }
            }

        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton lightBulbButton = (ImageButton) findViewById(R.id.lightBulbButton);
        lightBulbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRelayEnabled = !isRelayEnabled;
                String value = "off";

                if (isRelayEnabled) {
                    value = "on";
                    lightBulbButton.setImageResource(R.drawable.lightbulb);
                } else {
                    lightBulbButton.setImageResource(R.drawable.lightbulbdark);
                }
                getHttpRequest(value);
            }
        });

    }
}
