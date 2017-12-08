package com.hst.mininurse.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hst.mininurse.FileUtil;
import com.hst.mininurse.Ft311URTUtils;
import com.hst.mininurse.OnUsbDataReceiveListener;
import com.hst.mininurse.R;
import com.hst.mininurse.SocketUtil;
import com.hst.mininurse.bean.CodeAndMsgBean;
import com.hst.mininurse.constants.AppConstants;
import com.hst.mininurse.jsinterface.FT311UARTInterface;
import com.hst.mininurse.utils.BaseCallBack;
import com.hst.mininurse.utils.DESUtil2;
import com.hst.mininurse.utils.HstDialog;
import com.hst.mininurse.utils.SPUtils;
import com.hst.mininurse.utils.XmlParseUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/8/29
 */

public class MainActivity2 extends AppCompatActivity {
    private String TAG = MainActivity2.class.getSimpleName();
    private WebView mWebView;
    private String mS;//配置数据

    public FT311UARTInterface uartInterface;
    public com.hst.mininurse.Ft311URTUtils Ft311URTUtils;
    public static boolean bConfiged = false;
    int prote;//波特率
    int parity = 0; //校验
    SharedPreferences mySharedPreferences;
    String appVersion="";
    MyBroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastReceiver=new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(FT311UARTInterface.action);
        registerReceiver(broadcastReceiver, filter);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // // 设置全屏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
//        AndroidBug5497Workaround.assistActivity(this);
        this.instance = this;
        init();
        initWebView();
        downFile();
        initState();
//        parseXml();
    }
/*
* 初始化终端状态
* */
    private void initState() {
    }

    void init() {
        appVersion=getAppVersionName(MainActivity2.this);
        uartInterface = new FT311UARTInterface(this);
        Ft311URTUtils = new Ft311URTUtils(uartInterface);

        // 实例化SharedPreferences对象（第一步）
        mySharedPreferences = getSharedPreferences(AppConstants.SHARE_PREF_FILENAME,
                Activity.MODE_PRIVATE);
        boolean islogined = mySharedPreferences.getBoolean(AppConstants.SHARE_PREF_LOGIND, false);
//       islogined=true;
        if (!islogined) {
            gotoLogin();
        } else {
//            prote = mySharedPreferences.getInt(AppConstants.SHARE_PREF_PROTE, 115200);
//            openConnect(prote + "");
        }

//        Toast.makeText(this, "设置波特率" + prote + bConfiged, Toast.LENGTH_SHORT).show();
    }

  class  MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
           Toast.makeText(MainActivity2.this,"获取设备状态",5).show();

            int[] states=intent.getExtras().getIntArray("data");
            if(states[0]==0){
            mHandler.sendEmptyMessage(6);
            }
            if(states[0]==1){
                mHandler.sendEmptyMessage(7);
            }
            if(states[1]==0){
                mHandler.sendEmptyMessage(8);
            }
            if(states[1]==1){
                mHandler.sendEmptyMessage(9);
            }
        }
    };
    public static String getAppVersionName(Context context) {
        int versionCode=0;
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode=pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName+"."+versionCode;
    }


    /**
     * 跳转登录
     */
    void gotoLogin() {
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
        finish();
        System.exit(0);
    }

    // 打开USB连接，设置波特率
    @JavascriptInterface
    public void openConnect(String bote1) {
        int bote = 0;
        prote = bote;
        try {
            bote = Integer.parseInt(bote1);
            // 实例化SharedPreferences.Editor对象（第二步）
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            // 用putString的方法保存数据
            editor.putInt("prote", bote);
            // 提交当前数据
            editor.commit();

            int parity = mySharedPreferences.getInt(AppConstants.SHARE_PREF_PARITY, 0);
            if (bConfiged) {
//                uartInterface.DestroyAccessory(bConfiged);
                bConfiged = uartInterface.SetConfig(bote, (byte) 8, (byte) 1, (byte) parity,
                        (byte) 0);

            } else {
                bConfiged = uartInterface.SetConfig(bote, (byte) 8, (byte) 1, (byte) parity,
                        (byte) 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "设置波特率" + bote + bConfiged, Toast.LENGTH_SHORT).show();
    }

    /**
     * 当界面设置波特率按钮点击执行
     */
    @JavascriptInterface
    public void onBtnBoteClick() {
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.Dialog).create(); // 先得到构造器
        builder.show();
        builder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        builder.getWindow().setContentView(R.layout.bote_dialog);
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.bote_dialog, null);
        builder.getWindow().setContentView(view);
        final TextView et_bote = (TextView) view.findViewById(R.id.et_bote);
        final TextView et_parity = (TextView) view.findViewById(R.id.et_parity);
        Button btn = (Button) view.findViewById(R.id.btn);
        et_bote.setText((String) SPUtils.get(MainActivity2.this, "bote", " "));
        et_parity.setText((String) SPUtils.get(MainActivity2.this, "parity", " "));

        et_bote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {"9600", "115200"};
                ShowChoise((TextView) v, items);
            }
        });

        et_parity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  String[] items = {"none", "odd", "even"};
                String[] items = {"none", "odd"};
                ShowChoise((TextView) v, items);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bote = 0;
                byte parity = 0;
                String trim = "";
                try {
                    bote = Integer.parseInt(et_bote.getText().toString().trim());
                    trim = et_parity.getText().toString().trim();
                    if ("none".equals(trim)) {
                        parity = 0;
                    } else if ("odd".equals(trim)) {
                        parity = 1;
                    }
                    //        else if ("even".equals(trim)) {
                    //          parity = 2;
                    //}
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                SPUtils.put(MainActivity2.this, "bote", bote + "");
                SPUtils.put(MainActivity2.this, "parity", trim + "");

                boolean b = uartInterface.SetConfig(bote, (byte) 8, (byte) 1, (byte) parity,
                        (byte) 0);
                bConfiged = b;
                if (b) {
                    prote = bote;
                    MainActivity2.this.parity = parity;
                    mHandler.sendEmptyMessage(3);
                    checkOldDevice();
                } else {
                    Toast.makeText(MainActivity2.this, "请链接USB", Toast.LENGTH_SHORT).show();
                }
                builder.dismiss();
            }
        });

    }

    private void checkOldDevice() {
        Ft311URTUtils.writeData("verinf");

        Ft311URTUtils
                .OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
                    @Override
                    public void onUsbDataReceive(String data) {
                        if (data.contains("cmdack=") || data.contains("^end")) {
                            Toast.makeText(MainActivity2.this, "新设备", Toast.LENGTH_SHORT).show();
                            Ft311URTUtils.isOldDevice = false;
                        } else {
                            Toast.makeText(MainActivity2.this, "老设备", Toast.LENGTH_SHORT).show();
                            Ft311URTUtils.isOldDevice = true;
                        }
                    }
                });
    }

    /**
     * 选择IP diaolog
     *
     * @param view
     * @param items
     */
    private void ShowChoise(final TextView view, final String[] items) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("请选择");
        //    设置一个下拉的列表选择项
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(MainActivity2.this, "选择的城市为：" + cities[which], Toast.LENGTH_SHORT).show();
                view.setText(items[which]);
            }
        });
        builder.show();
    }


    // 发送数据到终端(写入)
    StringBuffer mReturnData = new StringBuffer();//返回给UI的数据
    Boolean mIsFirstTime = true;//是否是读或写操作的第一个循环

    /**
     * 往串口写入数据
     *
     * @param datas
     * @param iswrite
     */
    public void sendUsbDatabyW(final List<String> datas, final Boolean iswrite) {
        Log.i(TAG, "prote----" + prote);
        if (!canWrite) {
            Toast.makeText(this, "正在等待终端收据返回,请稍后再进行操作", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsFirstTime) {
            if (!bConfiged) {
                Toast.makeText(this, "请设置波特率", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessage(-1);
                return;
            }
            mReturnData = new StringBuffer();
            mIsFirstTime = false;
        }
        String data = datas.get(0);
//        data="pramrd=00000013";
        FileUtil.write2Filelog("MainActivity2:sendUsbDatabyW:data1:" + data);

        if (data.length() != 0x00) {


//            HstDialog.showDialog(MainActivity2.this,"write to terminal",data);
            FileUtil.write2Filelog("MainActivity2:sendUsbDatabyW:data2:" + data);
            if (Ft311URTUtils.isReadDataThreadStart) {
                HstDialog.showDialog(MainActivity2.this, "提示：", "正在读数据，请稍等");
                return;
            }
            Ft311URTUtils.writeData(data);
            Ft311URTUtils
                    .OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
                        @Override
                        public void onUsbDataReceive(String data) {
                            if (data == null) {
                                data = "null";
                            }
                            if (data.contains("cmdack=")) data =data.replace("cmdack=", "");
                            if (data.contains("^end")) data = data.replace("^end", "");
                            Log.w("text", "data"+ data);

//                            FileUtil.write2Filelog("MainActivity2:sendUsbDatabyW:data3:" + data);
                            if (iswrite)//向终端写数据
                            {
                                if (datas.size() > 0)
                                    datas.remove(0);
                                if (datas.size() > 0) {
                                    mReturnData.append(data).append(";,;");
//                                    HstDialog.showDialog(MainActivity2.this,"终端返回",mReturnData.toString());
                                    sendUsbDatabyW(datas, true);
                                } else {
                                    mReturnData.append(data);
//                                    HstDialog.showDialog(MainActivity2.this,"终端返回",mReturnData.toString());
                                    String url = "javascript:sendWriteData('"
                                            + mReturnData + "')";
                                    mWebView.loadUrl(url);
                                    onReadComplete();
                                }
                            } else {//读数据
                                if (datas.size() > 0)
                                    datas.remove(0);
                                if (datas.size() > 0) {
                                    mReturnData.append(data).append(";,;");
//                                    HstDialog.showDialog(MainActivity2.this,"终端返回",mReturnData.toString());
                                    sendUsbDatabyW(datas, false);
                                } else {
                                    mReturnData.append(data);
//                                    HstDialog.showDialog(MainActivity2.this,"终端返回",mReturnData.toString());
                                    String url = "javascript:sendQueryData('"
                                            + mReturnData + "')";
                                    FileUtil.write4Filelog("mReturnData:"+mReturnData);
                                    mWebView.loadUrl(url);
                                    onReadComplete();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 下载配置文件
     */
    private void downFile() {
        try {
            OkHttpUtils
                    .get()
                    .url(AppConstants.login)
                    .build()
                    .execute(new BaseCallBack(this) {
                        @Override
                        public void onResponse(String response, int id) {
                            if (TextUtils.isEmpty(response)) {
                                return;
                            }
                            try {
                                String result = DESUtil2.DecryptDoNet(response, AppConstants.DESKEY);
                                CodeAndMsgBean codeAndMsgBean = new Gson().fromJson(result, CodeAndMsgBean.class);
                                if (0 == codeAndMsgBean.getReturnCode()) {
                                    InputStream is = new ByteArrayInputStream(codeAndMsgBean.getReturnMessage().trim().getBytes());
                                    mS = XmlParseUtil.xmlToBean(is);
                                    Log.d("elliotlin","mS:"+mS);
//                                    Log.w("MainActivity2", mS);
//                                    HstDialog.showDialog(MainActivity2.this,"服务器返回",mS);
//                                    FileUtil.write2Filelog("服务器返回"+mS);
                                    mWebView.loadUrl("javascript:sendData('" + mS + "')");
                                } else {
                                    Toast.makeText(mContext, codeAndMsgBean.getReturnMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("RegisterActivity", e.getMessage());
        }


    }

//    void write2Filelog(String str){
//        FileUtil.writeStr2File(str,Environment
//                    .getExternalStorageDirectory().getAbsolutePath()
//                    + "/Hst_log.txt");
//
//    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
//        AssetManager assets = getAssets();
//        try {
//            InputStream open = assets.open("WisdomDetectionToo.Config");
//            mS = XmlParseUtil.xmlToBean(open);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mWebView = (WebView) findViewById(R.id.webview);

//        webview.loadUrl("http://www.bbaidu.com/");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d("webview", message + " -- From line " + lineNumber + " of " + sourceID);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                Log.d("elliotlin","onJsAlert");//alert( "acceptTag" +'-'+idtitle + '-' + opt);
               if(message.contains("acceptTag")){
                   String[] args=message.split("-");
                   final AlertDialog builder = new AlertDialog.Builder(MainActivity2.this).create(); // 先得到构造器
                   builder.show();
                   builder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                   builder.getWindow().setContentView(R.layout.content_dialog);
                   LayoutInflater factory = LayoutInflater.from(MainActivity2.this);
                   View view2 = factory.inflate(R.layout.content_dialog, null);
                   builder.getWindow().setContentView(view2);
                   final TextView title= (TextView) view2.findViewById(R.id.tvtitle0);
                   final EditText etOld= (EditText) view2.findViewById(R.id.etContentOld);
                   etOld.setFocusable(false);
                   etOld.setFocusableInTouchMode(false);
                   final EditText etNew= (EditText) view2.findViewById(R.id.etContentNew);
                   final Button btSure= (Button) view2.findViewById(R.id.btn2);
                   final Button btCancel= (Button) view2.findViewById(R.id.btn1);
                   if(args.length==3){
                       title.setText(args[1]);
                       if(args[2].equals("false")){//隐藏
                           etNew.setVisibility(View.GONE);
                           btCancel.setVisibility(View.GONE);
                       }
                   }else if(args.length==4){
                       title.setText(args[1]);
                       etOld.setText(args[3]);
                   }else{
                       builder.dismiss();
                       Toast.makeText(MainActivity2.this,"未知错误！",5).show();
                   }

                   btCancel.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           builder.dismiss();
                       }
                   });

                   btSure.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           String newContent=etNew.getText().toString();
                           if((newContent!=null)&&(!newContent.equals(""))){
                               mWebView.loadUrl("javascript:reciveEditText('" + newContent + "')");
                               builder.dismiss();
                           }

                       }
                   });

               }
                if("searchVersion".equals(message)){
                    new AlertDialog.Builder(MainActivity2.this).setTitle("温馨提示")//设置对话框标题
                            .setMessage("\n\r\r\r版本号："+appVersion)//设置显示的内容
                            .setPositiveButton("确定",null).show();//在按键响应事件中显示此对话框
                }/*else if("showLocation".equals(message)){
                    new AlertDialog.Builder(MainActivity2.this).setTitle("温馨提示")//设置对话框标题
                            .setMessage("定位")//设置显示的内容
                            .setPositiveButton("确定",null).show();//在按键响应事件中显示此对话框
                }else if("ifOnline".equals(message)) {
                    new AlertDialog.Builder(MainActivity2.this).setTitle("温馨提示")//设置对话框标题
                            .setMessage("是否在线")//设置显示的内容
                            .setPositiveButton("确定", null).show();//在按键响应事件中显示此对话框
                }*/else {

                }
                result.cancel();
                return true;
            }

        });
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity2.this, "加载错误", Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                String data = "{ \"name\": \"Luo\",\"age\": \"18\"}";
//                view.loadUrl("javascript:doo('"+ data+"')");
                view.loadUrl("javascript:sendData('" + mS + "')");

//                Toast.makeText(MainActivity.this, "页面加载完成", Toast.LENGTH_SHORT).show();
                super.onPageFinished(view, url);
            }
        });
        mWebView.addJavascriptInterface(this, "android");
        mWebView.loadUrl("file:///android_asset/nurse/index2.html");
    }

    /**
     * 解析xml
     */
    private void parseXml() {
        AssetManager assets = getAssets();
        try {
            InputStream open = assets.open("WisdomDetectionToo.Config");
            mS = XmlParseUtil.xmlToBean(open);
//            FileUtil.write2Filelog("\r\n"+mS+"\r\n");
//            HstDialog.showDialog(this,"d服务器返回",mS);
            mWebView.loadUrl("javascript:sendData('" + mS + "')");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 点击写入按钮执行
     *
     * @param orders
     */
    @JavascriptInterface
    public void write(String orders) {
        Ft311URTUtils.writeEmpty();


        String[] orderas0 = orders.split("\\;,;");
        final List orderas = new ArrayList<String>();
        for (int i = 0; i < orderas0.length; i++) {
            orderas.add(orderas0[i]);
        }
//        HstDialog.showDialog(MainActivity2.this,"H5返回len:",""+orderas.size());
        mIsFirstTime = true;

        Ft311URTUtils
                .OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
                    @Override
                    public void onUsbDataReceive(String data) {
                        sendUsbDatabyW(orderas, true);

                    }
                });


    }

    /**
     * 点击查询按钮执行
     *
     * @param orders
     */
    @JavascriptInterface
    public void query(String orders) {
        Ft311URTUtils.writeEmpty();

        FileUtil.write4Filelog("H5返回" + orders);
        String[] orderas0 = orders.split("\\;,;");
        final List orderas = new ArrayList<String>();
        for (int i = 0; i < orderas0.length; i++) {
            orderas.add(orderas0[i]);
        }
        mIsFirstTime = true;

        Ft311URTUtils
                .OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
                    @Override
                    public void onUsbDataReceive(String data) {
                        sendUsbDatabyW(orderas, false);
                    }
                });


    }


    SocketUtil mSocketUtil;


    public void onInitSocketRublish() {
        uartInterface.initRublist();
    }

    @JavascriptInterface
    public void btnSocketClick(int text) {
        if (1 == text) { //切换成传统模式
            mSocketUtil.closeSocket();
            uartInterface.sockOutputStream = null;
            mHandler.sendEmptyMessage(2);
        } else {
            bConfiged = uartInterface.SetConfig(prote, (byte) 8, (byte) 1, (byte) parity,
                    (byte) 0);
            if (!bConfiged) {
                HstDialog.showDialog(MainActivity2.this, "提示", "请重接USB");
                return;
            }
            creatIpDiaglog1();
        }
    }

    public static MainActivity2 instance;

    private  MyHandler mHandler = new MyHandler();
    public MyHandler getHandler() {
        return mHandler;
    }
   public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String s;
            String url = "javascript:setisRWdataFalse()";
            mWebView.loadUrl(url);
            switch (msg.what) {
                case 1:
                    mWebView.loadUrl("javascript:setBtnSocketText('" + 1 + "')");
                    break;
                case 2:
                    mWebView.loadUrl("javascript:setBtnSocketText('" + 2 + "')");
                    break;
                case 3:
                    mWebView.loadUrl("javascript:resetBtnBote()");

                    break;
                case 4:
                    String data = (String) msg.obj;
                    data = data.trim();
                    if (data != null && !TextUtils.isEmpty(data) && data.contains("apptxt=")) {
                        String[] split = data.split("=");
                        if (split.length == 2) {
                            Dialog dialog = new AlertDialog.Builder(MainActivity2.this)
                                    .setTitle("提示")                // 设置标题
                                    .setMessage(split[1])      // 显示信息
                                    .create();                      // 创建Dialog
                            dialog.show();
                        }
                    }

                    break;

                case 5:
                    readNumber++;
                    if (readNumber >= 5) {
                        canWrite = true;
                        readNumber = 0;
                        return;
                    }

                    Ft311URTUtils
                            .OnUsbDataReceiveListener(new OnUsbDataReceiveListener() {
                                @Override
                                public void onUsbDataReceive(String data) {
                                    data = data.trim();
                                    if (data != null && !"null".equals(data)) {
                                        Message message = mHandler.obtainMessage();
                                        message.what = 4;
                                        message.obj = data;
                                        mHandler.sendMessage(message);
                                        canWrite = true;
                                        readNumber = 0;
                                    } else {
                                        Ft311URTUtils.writeEmpty();
                                        mHandler.sendEmptyMessageDelayed(5, 1000);
                                    }
                                }
                            });
                    break;
                case 6:{
                    Toast.makeText(MainActivity2.this,"xx1",5).show();
                    mWebView.loadUrl("javascript:setUartLocation('" + 0 + "')");//定位否
                    break;
                }
                case 7:{
                    Toast.makeText(MainActivity2.this,"xx2",5).show();
                    mWebView.loadUrl("javascript:setUartLocation('" + 1 + "')");//定位
                    break;
                }
                case 8:{
                    Toast.makeText(MainActivity2.this,"xx3",5).show();
                    mWebView.loadUrl("javascript:setUartOnline('" + 0+ "')");//不在线
                    break;
                }
                case 9:{
                    Toast.makeText(MainActivity2.this,"xx4",5).show();
                    mWebView.loadUrl("javascript:setUartOnline('" + 1 + "')");//在线
                    break;
                }
            }
        }
    };

    /**
     * 开启socket
     *
     * @param ip
     * @param port
     */
    private void getSocket(String ip, int port) {

        if (mSocketUtil == null) mSocketUtil = new SocketUtil();
        mSocketUtil.getSocket(ip, port, true, new SocketUtil.OnOpenSocketSuccess() {
            @Override
            public void onOpenSocketSuccess(final  Socket socket) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String socketData = getDateFromSocket(socket);
                                Message message = new Message();
                                message.obj = socketData;
                                message.what = 1;
                                mHandler.sendMessage(message);
                            }
                            catch (Exception e){

                            }
                        }
                    }).start();



                } catch (Exception e) {
                    Toast.makeText(MainActivity2.this, "socket连接异常", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                //   mHandler.sendEmptyMessage(1);

            }

            @Override
            public void onOpenSocketFail(Exception e) {
                Looper.prepare();
                Toast.makeText(MainActivity2.this, "socket打开异常" + e.getMessage(), Toast.LENGTH_SHORT).show();
                mSocketUtil.closeSocket();
                Looper.loop();
            }
        });
    }

    /**
     * 当写入和查询结束的时候需要发送一个append指令
     */
    public void onReadComplete() {
        if (Ft311URTUtils.isOldDevice) {
            return;
        }

        Ft311URTUtils.writeData("append");
//        Ft311URTUtils.writeData("apptxt=xx");

        mHandler.sendEmptyMessage(5);
        canWrite = false;
    }

    public int readNumber = 0;

    public boolean canWrite = true;


    /**
     * 操作socket管道流
     *
     * @param serverSocket
     * @throws InterruptedException
     */
    public String getDateFromSocket(Socket serverSocket) throws InterruptedException {
        /* * * * * * * * * * Socket 客户端读取服务器端响应数据 * * * * * * * * * */
        try {
            // serverSocket.isConnected 代表是否连接成功过
            // 判断 Socket 是否处于连接状态
            if (true == serverSocket.isConnected() && false == serverSocket.isClosed()) {
                // 客户端接收服务器端的响应，读取服务器端向客户端的输入流
                InputStream inputStream = serverSocket.getInputStream();
                OutputStream outputStream = serverSocket.getOutputStream();
                uartInterface.SendPacket(inputStream);//将sock数据传给串口
                uartInterface.sockOutputStream = outputStream;


//                if (uartInterface.inputstream != null) {
//                    byte buffer[] = new byte[16 * 1024];
//                    int temp = 0;
//                    // 循环读取文件
//                    while ((temp = uartInterface.inputstream.read(buffer)) != -1) {
//                        // 把数据写入到OuputStream对象中
//                        outputStream.write(buffer, 0, temp);
//                        outputStream.flush();
//                    }
//                }

//                byte[] buffer = new byte[1024 * 4];
//                int num;
//
//                while ((num = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, num);
//                    outputStream.flush();
//                }


            }
            // 关闭网络
//            serverSocket.close();
        } catch (UnknownHostException e) {
            Log.w("RegisterActivity", "未知异常");
            e.printStackTrace();
            return"UnknownHostException";
        } catch (IOException e) {
            Log.w("RegisterActivity", "IO异常");
            e.printStackTrace();
            return"IOException";
        }
        return"";
    }

    /**
     * 创建波特率dialog
     */
    public void creatIpDiaglog1() {
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.Dialog).create(); // 先得到构造器
        builder.show();
        builder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        builder.getWindow().setContentView(R.layout.ip_dialog);
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.ip_dialog, null);
        builder.getWindow().setContentView(view);
        final EditText et_ip = (EditText) view.findViewById(R.id.et_ip);
        final EditText et_port = (EditText) view.findViewById(R.id.et_port);
        Button btn = (Button) view.findViewById(R.id.btn);
        et_ip.setText((String) SPUtils.get(MainActivity2.this, "ip", " "));
        et_port.setText((String) SPUtils.get(MainActivity2.this, "port", " "));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = et_ip.getText().toString().trim();
                int port = 0;
                try {
                    port = Integer.parseInt(et_port.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity2.this, "端口格式不正确", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(ip) || port == 0) {
                    Toast.makeText(MainActivity2.this, "请输入IP和端口", Toast.LENGTH_SHORT).show();
                    return;
                }
                SPUtils.put(MainActivity2.this, "ip", ip);
                SPUtils.put(MainActivity2.this, "port", port + "");

                getSocket(ip, port);
                builder.dismiss();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        FileUtil.write3Filelog("");

        uartInterface.ResumeAccessory();

    }

    @Override
    protected void onPause() {
      /*  mySharedPreferences = getSharedPreferences(AppConstants.SHARE_PREF_FILENAME,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(AppConstants.SHARE_PREF_LOGIND, false);
        editor.commit();*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
      /*  mySharedPreferences = getSharedPreferences(AppConstants.SHARE_PREF_FILENAME,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(AppConstants.SHARE_PREF_LOGIND, false);
        editor.commit();*/
        super.onDestroy();
        uartInterface.DestroyAccessory(false);

    }
}
