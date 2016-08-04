package com.niceweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.niceweather.app.db.NiceWeatherDB;
import com.niceweather.app.model.City;
import com.niceweather.app.model.County;
import com.niceweather.app.model.Province;

public class Utility {
	public synchronized static boolean handleProvincesResponse(
			NiceWeatherDB niceWeatherDB, String response) {
		if(!TextUtils.isEmpty(response)){
			Log.d("handleProvincesResponse",response);
			String[] allProvinces = response.split(",");
			for(String p:allProvinces){
				
				String[] array = p.split("\\|");
				Province province = new Province ();
				province.setProvinceCode(array[0]);
				province.setProvinceName(array[1]);
				niceWeatherDB.saveProvince(province);
			}
			return true;
		}
	
		return false;
	}
	/**
	*解析和处理服务器返回的市级数据
	*/
	public static boolean handleCitiesResponse(NiceWeatherDB niceWeatherDB,
		String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			Log.d("handleCitiesResponse",response);
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String c:allCities){
					String[]array=c.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//将解析出来的数据存储到City表
					niceWeatherDB.saveCity(city);
				}
			return true;
			}
		}
		return false;
	}
	public static boolean handleCountiesResponse(NiceWeatherDB niceWeatherDB,
			String response ,int cityId){
		if(!TextUtils.isEmpty(response)){
			Log.d("handleCountiesResponse",response);
			String[] allCounties = response.split(",");
			if(allCounties != null && allCounties.length >0){
				for(String c:allCounties){
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					niceWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	/**
	*解析服务器返回的JSON数据，并将解析出的数据存储到本地。
	*{"weatherinfo":
{"city":"昆山","cityid":"101190404","temp1":"21℃","temp2":"9℃",
"weather":"多云转小雨","img1":"d1.gif","img2":"n7.gif","ptime":"11:00"}
}
	*/
	public static void handleWeatherResponse(Context context,String response){
		try{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject  weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName  = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name",cityName);
		editor.putString("weather_code",weatherCode);
		editor.putString("temp1",temp1);
		editor.putString("temp2",temp2);
		editor.putString("weather_desp",weatherDesp);
		editor.putString("publish_time",publishTime);
		editor.putString("current_date",sdf.format(new Date()));
		editor.commit();
	}
}
