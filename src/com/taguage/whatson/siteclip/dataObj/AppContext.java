package com.taguage.whatson.siteclip.dataObj;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AppContext extends Application {

	SharedPreferences sp;

	//小米推送
	public static final String APP_ID = "2882303761517289396";
	public static final String APP_KEY = "5461728959396";
	public static final String TAG = "com.taguage.whatson.siteclip";

	@Override 
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(sp==null)sp=PreferenceManager.getDefaultSharedPreferences(this);
		Utils.getInstance().initUtils(this);	


		//小米推送
		//初始化push推送服务
		if(shouldInit()) {
			MiPushClient.registerPush(this, APP_ID, APP_KEY);
		}
		//打开Log
		LoggerInterface newLogger = new LoggerInterface() {

			@Override
			public void setTag(String tag) {
				// ignore   
				//MLog.e("","push tag="+tag);
			}

			@Override
			public void log(String content, Throwable t) {
				//Log.e("", "push--"+content, t);
			}

			@Override
			public void log(String content) {
				//MLog.e("", "push--"+content);
			}
		};
		Logger.setLogger(this, newLogger);

	}

	private boolean shouldInit() {
		ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		String mainProcessName = getPackageName();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid && mainProcessName.equals(info.processName)) {
				return true;
			}
		}
		return false;
	}

	

	//sharedpreference related funcs
	public int getSpInt(int key)
	{
		return sp.getInt(getString(key), -1);
	}
	public long getSpLong(int key)
	{
		return sp.getLong(getString(key), -1);
	}
	public String getSpString(int key)
	{
		return sp.getString(getString(key), "");
	}
	public  boolean getSpBoolean(int key)
	{
		return sp.getBoolean(getString(key), false);
	}
	public  void setSpInt(int key,int value)
	{
		sp.edit().putInt(getString(key), value).commit();
	}
	public  void setSpLong(int key,long value)
	{
		sp.edit().putLong(getString(key), value).commit();
	}
	public  void setSpString(int key,String value)
	{
		sp.edit().putString(getString(key), value).commit();
	}
	public  void setSpBoolean(int key,boolean value)
	{
		sp.edit().putBoolean(getString(key), value).commit();
	}
	public  void removeSpData(int key)
	{
		sp.edit().remove(getString(key)).commit();
	}
	public  void clearSpData()
	{
		sp.edit().clear().commit();
	}
}
