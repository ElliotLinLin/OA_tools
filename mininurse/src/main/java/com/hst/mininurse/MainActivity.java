package com.hst.mininurse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hst.mininurse.jsinterface.FT311UARTInterface;
import com.hst.mininurse.jsinterface.JSInterface;
import com.hst.mininurse.wifi.WifiHotUtil;

public class MainActivity extends Activity {
	private String TAG = MainActivity.class.getSimpleName();

	private Button open_service;
	private TextView receive_text;
	private Button close_service;
	private WebView mWebView;
	private Button send;
	private EditText input;
	private int mState;
	private WifiHotUtil wifiUtil;
	public FT311UARTInterface uartInterface;
	public Ft311URTUtils Ft311URTUtils;
	public static boolean bConfiged = false;
	// String temp="";
	int prote;
	SharedPreferences mySharedPreferences;
	String datatemp2 = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// // 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		AndroidBug5497Workaround.assistActivity(this);
		init();
		showDebugView(false);
	}

	private void init() {
		wifiUtil = new WifiHotUtil(this);
		uartInterface = new FT311UARTInterface(this);
		Ft311URTUtils = new Ft311URTUtils(uartInterface);
		wifiUtil.startWifiAp("SM-N9100", "52222100");// 强制设置手机wifi热点为指定热点
		open_service = (Button) findViewById(R.id.open_service);
		close_service = (Button) findViewById(R.id.close_service);
		send = (Button) findViewById(R.id.send);
		receive_text = (TextView) findViewById(R.id.receive_text);
		input = (EditText) findViewById(R.id.input);
		// input.setText("$PWISDOM,QV#");
		input.setText("reset");
		initWebView();
		// 实例化SharedPreferences对象（第一步）
		mySharedPreferences = getSharedPreferences("test",
				Activity.MODE_PRIVATE);
		prote = mySharedPreferences.getInt("prote", 115200);
		Log.i(TAG, "prote----" + prote);
		TCPService.getInstance().setOnConnStateChangeListener(
				new OnConnStateChangeListener() {
					@Override
					public void onChange(final int state) {
						mState = state;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String msg = "";
								switch (state) {
								case TCPService.STATE_CONNCTING:
									msg = "连接中";
									break;
								case TCPService.STATE_DISCONNCTED:
									msg = "连接断开";
									break;
								case TCPService.STATE_CONNCTED:
									msg = "连接成功";
									break;
								case TCPService.STATE_STOP:
									msg = "连接关闭";
									break;
								}
								String url = String.format(
										"javascript:isConnect('%s')", mState);
								mWebView.loadUrl(url);
								receive_text.setText("");
								open_service.setText("开启服务-" + msg);
							}
						});
					}
				});
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// String temp = input.getText().toString().trim();
				// if (TextUtils.isEmpty(temp)) {
				// Toast.makeText(getApplicationContext(), "输入内容",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				// TCPService.getInstance().sendData(temp.getBytes());
				// sendUsbData(input.getText().toString());
				// input.setText(sendUsbData(input.getText().toString()));
			}
		});
		close_service.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 关闭服务
				TCPService.getInstance().close();
			}
		});
		open_service.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startSocketService();
			}

		});
	}

	public void setdate() {
		
		try {
			datatemp2 = FileUtil.readSDFile(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/pram_fat.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("qqqqq", "data1===" + datatemp2);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (datatemp2 != null) {
					// java调用js
					String url = "javascript:getMessageOfAndroid('" + datatemp2 + "','" + prote
							+ "','" + 2 + "')";
					mWebView.loadUrl(url);
				}	
			}
		});
		
	}

	// 取到本机的文件
	public void openConnect(int bote) {
		// 实例化SharedPreferences.Editor对象（第二步）
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		// 用putString的方法保存数据
		editor.putInt("prote", bote);
		// 提交当前数据
		editor.commit();
		if (bConfiged) {
			uartInterface.DestroyAccessory(bConfiged);
			uartInterface.SetConfig(prote, (byte) 8, (byte) 1, (byte) 0,
					(byte) 0);
			bConfiged = true;
		}
	}

	// 取到本机的文件
	public void sendUsbData(String data, final int position, final int write) {
		Log.i(TAG, "prote----" + prote);
		Log.i(TAG, "position----" + position);
		Log.i(TAG, "write----" + write);
		Log.i(TAG, "data----" + data+"");
		FileUtil.write2Filelog("MainActivity:sendUsbData:data0:"+data);
		
		if (data.length() != 0x00) {
			if (!bConfiged) {
				uartInterface.SetConfig(prote, (byte) 8, (byte) 1, (byte) 0,
						(byte) 0);
				bConfiged = true;
			}
			Ft311URTUtils.writeData(data);
			Ft311URTUtils
					.OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
						@Override
						public void onUsbDataReceive(String data) {
							FileUtil.write2Filelog("MainActivity:sendUsbData:receivedata:"+data);
							if (data != null) {
								// java调用js
								String url = "javascript:getMessageOfAndroid('"
										+ data + "','" + position + "','" + write
										+ "')";
								mWebView.loadUrl(url);
							}
						}
					});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检查Wifi热点是否按指定方式开启
		if (!wifiUtil.isWifiApEnabled()) {
			Toast.makeText(this, "wifi热点没有开启", Toast.LENGTH_SHORT).show();
			TCPService.getInstance().close();
		} else {
			// 开启，检查wifi名称和密码是否符合规范
			if (TCPService.getInstance().getConnectState() != TCPService.STATE_CONNCTING) {
				// TCPService.getInstance().start(Constant.SERVER_PORT);
			}
//			ActivityCompat.
			// Caused by: java.lang.SecurityException: com.hst.mininurse was not granted  this permission: android.permission.WRITE_SETTINGS.

			uartInterface.ResumeAccessory();// )

		}

	}

	private void startSocketService() {
		TCPService service = TCPService.getInstance();
		service.setOnReceiveListener(new OnDataReceiverListener() {

			@Override
			public void onReceive(final byte[] data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							String temp = new String(data, "gbk");
							// java调用js
							String url = String.format(
									"javascript:funFromjs('%s')", temp);
							receive_text.setText(new String(temp));
							mWebView.loadUrl(url);
						} catch (Exception ea) {
						}
					}
				});
			}
		});
		service.start(6000);
	}

	public void showDebugView(final boolean flag) {

		runOnUiThread(new Runnable() {
			public void run() {
				int v = flag ? View.VISIBLE : View.GONE;
				open_service.setVisibility(v);
				close_service.setVisibility(v);
				send.setVisibility(v);
				receive_text.setVisibility(v);
				input.setVisibility(v);
			}
		});
	}

	private void initWebView() {
		// String url = "file:/mnt/sdcard/hst_mininurse.html";// SD
		String urlAsset = "file:///android_asset/nurse/index.html";
		File file = Environment.getExternalStorageDirectory();
		Log.e("path", file == null ? null : file.toString());
		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings setting = mWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setJavaScriptCanOpenWindowsAutomatically(true);
		setting.setDefaultTextEncodingName("UTF-8");
		setting.setDomStorageEnabled(true);
		setting.setAllowFileAccess(true);
		setting.setSupportZoom(true);
		setting.setBuiltInZoomControls(true);
		// 开启缓存
		setting.setAppCacheEnabled(true);
		File cacheDir = getCacheDir();
		setting.setAppCachePath(cacheDir.getAbsolutePath());
		setting.setAppCacheMaxSize(1024 * 1024 * 200);

		setting.setCacheMode(WebSettings.LOAD_DEFAULT);
		setting.setDatabaseEnabled(true);
		setting.setDomStorageEnabled(true);

		mWebView.setScrollBarStyle(0);
		mWebView.addJavascriptInterface(new JSInterface(MainActivity.this),
				"android");
		mWebView.loadUrl(urlAsset);
		// mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						MainActivity.this);
				b.setTitle("提示信息");
				b.setMessage(message);
				b.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						});
				b.setCancelable(false);
				b.create().show();
				return true;
			}
		});
		mWebView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& mWebView.canGoBack()) {
						mWebView.goBack();
						return true;
					}
				}
				return false;
			}
		});
		mWebView.postDelayed(new Runnable() {

			@Override
			public void run() {
				startSocketService();
			}
		}, 2000);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		TCPService.getInstance().close();
		uartInterface.DestroyAccessory(false);
	}
}
