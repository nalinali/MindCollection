package com.taguage.whatson.siteclip.widget;

import com.taguage.whatson.siteclip.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class SaveWidget extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		RemoteViews rv=new RemoteViews(context.getPackageName(), R.layout.widget_save);
		
		appWidgetManager.updateAppWidget(appWidgetIds, rv); 		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	
}
