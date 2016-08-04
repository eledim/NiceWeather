package com.niceweather.service;

import com.niceweather.app.receiver.AutoUpdateReceiver;
import com.niceweather.util.HttpCallbackListener;
import com.niceweather.util.HttpUtil;
import com.niceweather.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public int onStartCommand(Intent intent,int flags,int startId){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}


			
		}).start();
		AlarmManager manager =(AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour =8*60*60*1000;
		long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
		
	}
	private void updateWeather() {
		// TODO Auto-generated method stub
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/"+
				weatherCode+".html";
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
			
			public void onFinish(String response){
				Utility.handleWeatherResponse(AutoUpdateService.this,
			response);
			}
		
			public void onError(Exception e){
			e.printStackTrace();
			}
		});
	
	}
}
