package com.niceweather.app.activity;


import java.util.ArrayList;
import java.util.List;



import com.niceweather.app.R;
import com.niceweather.app.db.NiceWeatherDB;
import com.niceweather.app.model.City;
import com.niceweather.app.model.County;
import com.niceweather.app.model.Province;
import com.niceweather.util.HttpCallbackListener;
import com.niceweather.util.HttpUtil;
import com.niceweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private ListView listView;
	private TextView titleText;
	private ArrayAdapter<String> adapter;
	private NiceWeatherDB niceWeatherDB;
	
	private List<String> dataList = new ArrayList<String>();
	private List<City> cityList;
	private List<County> countyList;
	private List<Province> provinceList;
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	private int currentLevel;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		listView = (ListView)findViewById(R.id.list_view);
		titleText =(TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		niceWeatherDB =NiceWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
		
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
				// TODO Auto-generated method stub
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					Log.d("onItemClick", "queryCities");
					queryCities();
				}else if(currentLevel ==LEVEL_CITY){
					selectedCity  = cityList.get(index);
					queryCounties();
				}//else if (currentLevel == LEVEL_COUNTY) {
					//String countyCode = countyList.get(index).getCountyCode();
					//Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					//intent.putExtra("county_code", countyCode);
					//startActivity(intent);
					//finish();
				//}
			} 


		});
		queryProvinces();
	}
	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList = niceWeatherDB.loadProvince();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
		
	}
	private void queryCities() {
		// TODO Auto-generated method stub
		Log.d("queryCities", "进入loadCities");
		cityList = niceWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			Log.d("queryCities", "size >0");
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			Log.d("queryCities", "进入queryFromServer");
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	private void queryCounties() {
		// TODO Auto-generated method stub
		countyList =niceWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(niceWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(niceWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(niceWeatherDB,
							response, selectedCity.getId());
				}
				Log.d("queryFromServer onFinish", response);
				if (result) {
					
					// 閫氳繃runOnUiThread()鏂规硶鍥炲埌涓荤嚎绋嬪鐞嗛�昏緫
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			public void onError(Exception e) {
				// 閫氳繃runOnUiThread()鏂规硶鍥炲埌涓荤嚎绋嬪鐞嗛�昏緫
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,
										"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("姝ｅ湪鍔犺浇...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 鍏抽棴杩涘害瀵硅瘽妗�
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 鎹曡幏Back鎸夐敭锛屾牴鎹綋鍓嶇殑绾у埆鏉ュ垽鏂紝姝ゆ椂搴旇杩斿洖甯傚垪琛ㄣ�佺渷鍒楄〃銆佽繕鏄洿鎺ラ��鍑恒��
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			
			finish();
		}
	}
}
