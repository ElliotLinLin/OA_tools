package com.hst.mininurse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hst.mininurse.app.Constant;

/**
 * 作者:Ljj
 * 名称:
 * 说明
 */
public class LogPrint {
	private static int contralKey = Constant.KEY_WRITE_LOG;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH-mm-ss", Locale.CHINA);
 
	/**打印调试日志
	 * @param text
	 */
	public static void print_e(String tag, Object msg, String cause, Context context) {
		int debug = contralKey;//1打印到控制台，2打印到文件，0不打印
		switch (debug) {
		case 0:
			break;
		case 1:
			Log.e(tag, cause + "--" + msg);
			break;
		case 2:
			if (msg == null) {
				return;
			}
			print_error_logfile(cause, msg.toString());
			break;
		default:
			break;
		}
	}


	/**打印错误日志
	 * @param cause
	 * @param hexMsg
	 */
	public static void print_error_mcu(String cause, String hexMsg) {
		String dir = getLogDir();
		if(TextUtils.isEmpty(dir)){
			return;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		// 保存文件
		String name = "MCU";
		writeFile(cause, hexMsg, dirFile, name);
	}

	/**打印错误日志
	 * @param cause
	 * @param hexMsg
	 */
	public static void print_error_logfile(String cause, String hexMsg) {
		String dir = getLogDir();
		if(TextUtils.isEmpty(dir)){
			return;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		writeFile(cause, hexMsg, dirFile, "");//写入文件
	}

	private static void writeFile(String cause, String hexMsg, File dirFile, String fileName) {
		Date current = new Date();
		String yyMMdd = dateFormat.format(current);
		String time = timeFormat.format(current);
		File logFile = new File(dirFile, fileName + " " + yyMMdd + ".log");//文件
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
				FileWriter writer = new FileWriter(logFile, logFile.exists());
				writer.append(current.getTime() + "\r\n");
				writer.append(time + "---" + cause + ":");
				writer.append("\r\n");
				writer.append(hexMsg);
				writer.close();
			} else {
				FileWriter writer = new FileWriter(logFile, logFile.exists());
				writer.append("\r\n");
				writer.append(time + "---" + cause + ":");
				writer.append("\r\n");
				writer.append(hexMsg);
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearLog() {
		String dir = getLogDir();
		if(TextUtils.isEmpty(dir)){
			return;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			return;
		} else {
			deleteFile(dirFile);
		}
	}
	
	/**清理几天前的log
	 * @param days
	 */
	public static boolean clearOldLog(int days){
		if(days<0){
			return true;
		}
		String dir = getLogDir();
		if(TextUtils.isEmpty(dir)){
			return true;
		}
		File f=new File(dir);
		if(!f.exists()){
			return true;
		}
		File[] listFiles = f.listFiles();
		if(listFiles!=null && listFiles.length>0){
			for(int i=0;i<listFiles.length;i++){
				File temp = listFiles[i];
				//读取第一行
				try {
					BufferedReader reader=new BufferedReader(new FileReader(temp));
					String line = reader.readLine();
					reader.close();
					long time = Long.parseLong(line);
					if(System.currentTimeMillis()>time+days*24*60*60*1000){
						//过期文件，删除
						deleteFile(temp);
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	public static void deleteFile(File file) {
		if (file.exists()) {//判断文件是否存在  
			if (file.isFile()) {//判断是否是文件  
				file.delete();//删除文件   
			} else if (file.isDirectory()) {//否则如果它是一个目录  
				File[] files = file.listFiles();//声明目录下所有的文件 files[];  
				for (int i = 0; i < files.length; i++) {//遍历目录下所有的文件  
					deleteFile(files[i]);//把每个文件用这个方法进行迭代  
				}
				file.delete();//删除文件夹  
			}
		} else {
			System.out.println("所删除的文件不存在");
		}
	}

	public static void print_error_logfile(String cause, String hexMsg, String fileName) {
		String dir = getLogDir();
		if(TextUtils.isEmpty(dir)){
			return;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		writeFile(cause, hexMsg, dirFile, fileName);
	}
	/**Log文件地址:SD卡下的wisdomLog文件夹下
	 * @return
	 */
	private static String getLogDir(){
		String externalStoragePath = FileUtil.getExternalStoragePath();
		if (externalStoragePath == null) {
			return "";
		}
		return externalStoragePath + "/wisdomLog";
	}
}
