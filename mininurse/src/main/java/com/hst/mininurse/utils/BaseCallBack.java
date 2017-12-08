package com.hst.mininurse.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/8/30
 */

public abstract class BaseCallBack extends StringCallback {
    // 圆形进度条
    protected ProgressDialog progressDialog = null;
    public Context mContext;
    public BaseCallBack(Context context) {
        mContext = context;
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        Toast.makeText(mContext, "网络异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    public abstract void onResponse(String response, int id);

    @Override
    public void onBefore(Request request, int id) {
        showProgressDiag();
    }

    @Override
    public void onAfter(int id) {
        closeDialog();
    }

    /**
     * 关闭对话框
     */
    protected void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showProgressDiag() {
        // 进度
        progressDialog = new ProgressDialog(mContext);

        // 是否允许关闭
        progressDialog.setCancelable(true);

        // 点击允许点击外部关闭对话框？
        progressDialog.setCanceledOnTouchOutside(false);

        // 显示对话框
        progressDialog.show();
        progressDialog.setContentView(CreateLoadingView.createView(mContext, ""));

    }
}
