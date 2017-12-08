package com.hst.mininurse.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Author:lsh
 * Version: 1.0
 * Description:BaseActivity暂时版本项目简单 界面少  留一个base备用
 * Date: 2017/8/28
 */


public class BaseActivity extends AppCompatActivity{

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



}
