package com.sinusgear.iot.lampapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class LampWidgetProvider extends AppWidgetProvider {

    private static final String TAG = LampWidgetProvider.class.getCanonicalName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lamp_widget);
            views.setOnClickPendingIntent(R.id.lightbulbSwitch, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(LampMqttService.MQTT_RECEIVED_ACTION)) {
            String value = intent.getStringExtra("value");

            Log.d(TAG, "Received value: " + value);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lamp_widget);

            if (value.equals("on")) {
                views.setImageViewResource(R.id.lightbulbSwitch, R.drawable.lightbulb);
            } else {
                views.setImageViewResource(R.id.lightbulbSwitch, R.drawable.lightbulbdark);
            }
            ComponentName thisWidget = new ComponentName( context, LampWidgetProvider.class );
            AppWidgetManager.getInstance( context ).updateAppWidget( thisWidget, views );
        }
    }


}
