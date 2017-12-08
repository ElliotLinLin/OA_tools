package com.hst.mininurse.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyApplication extends Application {
	private static Context mContext;
	@Override
	public void onCreate() {
		super.onCreate();
//		WifiHotUtil wifi=new WifiHotUtil(this);
//		String ssid=Constant.WIFI_NAME;
//		String passwd=Constant.WIFI_PASSWORD;
//		wifi.startWifiAp(ssid, passwd);
		mContext=this;
		CrashHandler.getInstance().init(this);
		String path = CrashHandler.getGlobalpath();
		Log.w("filepath", path);
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("okhttp"))
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(5, TimeUnit.SECONDS)
				//其他配置
				.build();

		OkHttpUtils.initClient(okHttpClient);
	}
	public static Context getAppContext(){
		return mContext;
	}
}
