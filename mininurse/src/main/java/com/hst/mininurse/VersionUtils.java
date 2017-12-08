package com.hst.mininurse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public final class VersionUtils
{
    private VersionUtils()
    {
    }
    
    /**
     * 版本是否在2.1之后（API 7）
     * @return
     */
    public static boolean isECLAIR_MR1(){
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR_MR1;
    }

    /**
     * 版本是否在2.2之后(API 8)
     * 
     * @return
     */
    public static boolean isrFroyo()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * 版本是否在2.3之后（API 9）
     * 
     * @return
     */
    public static boolean isGingerBread()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * 版本是否在4.0之后（API 14)
     * 
     * @return
     */
    public static boolean isIceScreamSandwich()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * 版本是否在3.0之后（API 11)
     * 
     * @return
     */
    public static boolean isHoneycomb()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    /**
     * 版本是否在3.2之后（API 13)
     * 
     * @return
     */
    public static boolean isHoneycombMR2()
    {
    	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }
    
    /**
     * 版本是否再4.1之后(API 16)
     * @return
     */
    public static boolean isJellyBean()
    {
    	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    

	/**
	 * 
	 * Android 2.2为 “Froyo”“冻酸奶”
	 * */
	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;//
	}

	/**
	 * Android 2.3为 Gingerbread（姜饼）
	 * */
	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	/**
	 * Android 3.0为 Honeycomb蜂巢
	 * */
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Android 3.1
	 * */
	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	/**
	 * Android 4.1. 果冻豆
	 * */
	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
    
	//获取当前版本号
	public  static String getCurrentVersion(Context context){
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo=null;
		 String mCurrentVersion ="";
		try {
			packageInfo = pm.getPackageInfo( context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  mCurrentVersion = packageInfo.versionName;
		  
		  return mCurrentVersion;
	}
    
    

}
