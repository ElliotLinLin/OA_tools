package com.hst.mininurse.jsinterface;

import java.io.IOException;

import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.hst.mininurse.FileUtil;
import com.hst.mininurse.LogPrint;
import com.hst.mininurse.MainActivity;
import com.hst.mininurse.TCPService;
import com.hst.mininurse.app.Constant;

public class JSInterface {
	private MainActivity mContxt;
	String temp = "";

	public JSInterface(MainActivity mContxt) {
		this.mContxt = mContxt;
	}

	/**
	 * 提供接口给JS调用，将数据发送给终端
	 * 
	 * @param data
	 */
	@JavascriptInterface
	public void sendData(String data) {
		TCPService.getInstance().sendData(data.getBytes());
	}

	/**
	 * 提供接口给JS调用，将数据发送给终端
	 * 
	 * @param data
	 */
	@JavascriptInterface
	public void sendData2() {
		mContxt.setdate();
	}

	// 选择波特率
	@JavascriptInterface
	public void Connect(int bo) {
		Log.i("lyq", "@JavascriptInterface--");
		Log.i("lyq", "prote-openConnect--"+bo);
		mContxt.openConnect(bo);
	}

	/**
	 * 提供接口给JS调用，将数据发送给终端
	 * 
	 * @param data
	 */
	@JavascriptInterface
	public void sendUsbData(String data, int position, int write) {
		Log.i("lyq", "JSInterface====");
		Log.i("lyq", "data====" + data);
		mContxt.sendUsbData(data, position, write);
//		mContxt.sendUsbData("reset", position, write);
	}

	/**
	 * @param data
	 *            数据
	 * @param tag
	 *            标题
	 * @param fileName
	 *            文件名称
	 */
	@JavascriptInterface
	public void writeLog(String data, String tag, String fileName) {
		LogPrint.print_error_logfile(tag, data, fileName);
	}

	/**
	 * 打开socket服务
	 */
	@JavascriptInterface
	public void openSocketService() {
		TCPService.getInstance().start(Constant.SERVER_PORT);
	}

	/**
	 * 关闭socket服务
	 */
	@JavascriptInterface
	public void closeSocketService() {
		TCPService.getInstance().close();
	}

	/**
	 * 显示调试界面
	 * 
	 * @param flag
	 */
	@JavascriptInterface
	public void showDebug(String flag) {
		mContxt.showDebugView("0".equals(flag));
	}
}
