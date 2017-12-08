package com.hst.mininurse.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hst.mininurse.R;
import com.hst.mininurse.bean.CodeAndMsgBean;
import com.hst.mininurse.constants.AppConstants;
import com.hst.mininurse.utils.BaseCallBack;
import com.hst.mininurse.utils.DESUtil2;
import com.hst.mininurse.utils.GetSystemInfoUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEt_pwd;
    private EditText mEt_imei;
    private View mIv_login;
    private View mIv_regsiter;
    private String mResult;
    private String mSzImei;
    private SharedPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_register);

        findView();
        checkVersion();
        boolean islogined = myPreferences.getBoolean(AppConstants.SHARE_PREF_LOGIND, false);
        Log.d("elliotlin","isLogin"+islogined);
     /*   if(islogined){
            startActivity(new Intent(RegisterActivity.this,MainActivity2.class));
            RegisterActivity.this.finish();
            return;
        }*/
    }
    /*
    * 核对版本
    * */
    private void checkVersion() {
        new AlertDialog.Builder(RegisterActivity.this).setTitle("版本更新")//设置对话框标题
                .setMessage("有新版本，请点击确定按钮更新！")//设置显示的内容
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       String apkUrl=AppConstants.basePath+"/UpLoadFiles/LoadWebConfigByApp/app.apk";
                      String downloadLocalPath=null;
                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                            downloadLocalPath=  Environment.getExternalStorageDirectory().getAbsolutePath()+"/HST_HELPER_DATA";
                            File f= new File(downloadLocalPath);
                            if(!f.exists()){
                                f.mkdirs();
                            }
                        }
                        if(downloadLocalPath==null){
                            Toast.makeText(RegisterActivity.this,"sd卡不可读写",5).show();
                        }
                        showDilogUpdate(apkUrl,downloadLocalPath);

                    }
                }).
                setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();//在按键响应事件中显示此对话框
    }

    private void showDilogUpdate(final String apkUrl,final  String downloadLocalPath) {

        final ProgressDialog pd;    //进度条对话框
        pd = new  ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //启动线程开始下载
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File file=getFileFromServe(apkUrl,downloadLocalPath, pd);
                    installApk(file);
                    pd.dismiss(); //结束掉进度条对话框
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void installApk(File file) {

        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);

    }

    private File getFileFromServe(String apkUrl,String downloadLocalPath, ProgressDialog pd) throws IOException {
        URL url=new URL(apkUrl);
        HttpURLConnection c= (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(5000);
        pd.setMax(c.getContentLength());
        InputStream is=c.getInputStream();
        long time=System.currentTimeMillis();
        File file=new File(downloadLocalPath,time+"_V"+MainActivity2.getAppVersionName(this)+".apk");
        FileOutputStream fos=new FileOutputStream(file);
        BufferedInputStream bis=new BufferedInputStream(is);
        byte[] buffer=new byte[1024];
        int len;
        int total=0;
        while((len=bis.read(buffer))!=-1){
            fos.write(buffer,0,len);
            total+=len;
            pd.setProgress(total);
        }
        fos.close();
        bis.close();
        is.close();
        return file;
    }

    private void findView() {
        // 实例化SharedPreferences对象（第一步）
        myPreferences = getSharedPreferences(AppConstants.SHARE_PREF_FILENAME,
                Activity.MODE_PRIVATE);
       // TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //mSzImei = TelephonyMgr.getDeviceId();
        if(Build.VERSION.SDK_INT>=21){//通过反射机制获取imei
            mSzImei=(String)GetSystemInfoUtil.getImeiAndMeid(this).get("imei1");
        }else{
            mSzImei=GetSystemInfoUtil.getImeiOrMeid(this);
        }
        mEt_pwd = (EditText) findViewById(R.id.et_pwd);
        mEt_imei=(EditText) findViewById(R.id.et_imei);
        mEt_imei.setText(mSzImei);
        Log.d("RegisterActivity",mSzImei);
        mIv_login = (findViewById(R.id.iv_login));
        mIv_regsiter = (findViewById(R.id.iv_regsiter));

        mIv_login.setOnClickListener(this);
        mIv_regsiter.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.iv_login:
                if (TextUtils.isEmpty(mSzImei)) {
                    showToast("设备号获取失败,请同意权限");
                    return;
                }
                postLoginOrRegiser(0);
//                setLogined();
//                startActivity(new Intent(RegisterActivity.this,MainActivity2.class));
//                RegisterActivity.this.finish();
                break;
            case R.id.iv_regsiter:
                postLoginOrRegiser(1);
                break;

        }
    }

    private void postLoginOrRegiser(int status) {
        try {
            OkHttpUtils
                    .get()
                    .url(AppConstants.login)
                    .addParams("IMEI", DESUtil2.EncryptAsDoNet(mSzImei, AppConstants.DESKEY))
                    .addParams("PassWord", mEt_pwd.getText().toString().trim())
                    .addParams("Status", status+"")
                    .build()
                    .execute(new BaseCallBack(this) {
                        @Override
                        public void onResponse(String response, int id) {
                            if (TextUtils.isEmpty(response)) {
                                return;
                            }
                            try {
                                String result = DESUtil2.DecryptDoNet(response, AppConstants.DESKEY);
                                Log.w("RegisterActivity", result);
                                CodeAndMsgBean codeAndMsgBean = new Gson().fromJson(result, CodeAndMsgBean.class);
                                if (0 == codeAndMsgBean.getReturnCode()) {
                                    setLogined();
                                    startActivity(new Intent(RegisterActivity.this,MainActivity2.class));
                                    RegisterActivity.this.finish();
                                } else {
                                    showToast(codeAndMsgBean.getReturnMessage());
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

    void setLogined(){

        SharedPreferences.Editor editor=myPreferences.edit();
        editor.putBoolean(AppConstants.SHARE_PREF_LOGIND,true);
        editor.commit();
    }
}
