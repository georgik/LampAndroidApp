package com.sinusgear.iot.lampapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    protected boolean isRelayEnabled = false;

    private void getHttpRequest(String state) {
        AsyncHttpClient asyncClient = new AsyncHttpClient();
        asyncClient.get("http://192.168.1.50/relay=" + state, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton lightBulbButton = (ImageButton) findViewById(R.id.lightBulbButton);
        lightBulbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRelayEnabled = !isRelayEnabled;
                String value = "off";

                if (isRelayEnabled) {
                    value = "on";
                }
                getHttpRequest(value);
            }
        });

    }
}
