package rocks.georgik.iot.lampapp;

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
    private static boolean isServiceRunning = false;
    final static String MQTT_TOGGLE_ACTION = "rocks.georgik.iot.lampapp.LampWidgetProvider.MQTT_TOGGLE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, LampWidgetProvider.class);
            intent.setAction(MQTT_TOGGLE_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lamp_widget);
            //views.setOnClickPendingIntent(R.id.lightbulbSwitch, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    /**
     * Receive message to update UI and change lightbulb icon according to value of message.
     * @param context
     * @param intent
     */
    public void updateWidget(Context context, Intent intent, RemoteViews views) {
        String value = intent.getStringExtra("value");

        Log.d(TAG, "Received value: " + value);

        if (value.equals("on")) {
            views.setImageViewResource(R.id.lightbulbSwitch, R.drawable.lightbulb);
        } else {
            views.setImageViewResource(R.id.lightbulbSwitch, R.drawable.lightbulbdark);
        }
        ComponentName thisWidget = new ComponentName( context, LampWidgetProvider.class );
        AppWidgetManager.getInstance( context ).updateAppWidget( thisWidget, views );
    }

    /**
     * Send request to service to toggle state of lightbulb.
     * @param context
     * @param intent
     * @param views
     */
    public void toggleLightbulb(Context context, Intent intent, RemoteViews views) {
        // Create a fresh intent
        if (!isServiceRunning) {
            isServiceRunning = true;
            Intent serviceIntent = new Intent(context, LampMqttService.class);
            serviceIntent.addCategory(Intent.CATEGORY_DEFAULT);
            serviceIntent.putExtra("value", "toggle");
            context.startService(serviceIntent);
        }


    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lamp_widget);

        if (action.equals(LampMqttService.MQTT_RECEIVED_ACTION)) {
            updateWidget(context, intent, views);
        } else if (action.equals(MQTT_TOGGLE_ACTION)) {
            toggleLightbulb(context, intent, views);
        }
        super.onReceive(context, intent);
    }


}
