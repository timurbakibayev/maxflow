package com.gii.maxflow;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Created by Timur_hnimdvi on 13-Sep-16.
 */
public class StandingsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            String textStandings = prefs.getString("widgetText","0 USD\n-----------\nTotal: 0 USD");

            if (textStandings.trim().equals(""))
                textStandings = "no data";

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_standings);
            remoteViews.setTextViewText(R.id.textView, textStandings);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, configPendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    String myData = "";

    @Override
    public void onReceive (Context context,
                    Intent intent) {
        super.onReceive(context,intent);
        myData = intent.getStringExtra("data");
    }
}