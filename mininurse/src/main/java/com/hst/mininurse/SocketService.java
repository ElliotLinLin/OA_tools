package com.hst.mininurse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/9/30
 */

public class SocketService extends Service{

    private SocketUtil mSocketUtil;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    @Override
    public void onCreate() {
        super.onCreate();




    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("++++++++++++onStartCommand++++++++++++");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("++++++++++++onDestroy++++++++++++");
        mSocketUtil.closeSocket();
        if (mInputStream != null) SocketUtil.closeStream(mInputStream);
        if (mOutputStream != null) SocketUtil.closeStream(mOutputStream);
        super.onDestroy();
    }




    public void getDateFromSocket(Socket serverSocket) throws InterruptedException {
        /* * * * * * * * * * Socket 客户端读取服务器端响应数据 * * * * * * * * * */
        try {
            // serverSocket.isConnected 代表是否连接成功过
            // 判断 Socket 是否处于连接状态
            if (true == serverSocket.isConnected() && false == serverSocket.isClosed()) {
                // 客户端接收服务器端的响应，读取服务器端向客户端的输入流
                mInputStream = serverSocket.getInputStream();
                mOutputStream = serverSocket.getOutputStream();
                byte[] buffer = new byte[1024 * 4];
                int num;

                while ((num = mInputStream.read(buffer)) != -1) {
                    mOutputStream.write(buffer, 0, num);
                    mOutputStream.flush();
                }


//                while (true) {
////                // 缓冲区
//                byte[] buffer = new byte[isRead.available()];
//                // 读取缓冲区
//                isRead.read(buffer);
//                // 转换为字符串
//                String responseInfo = new String(buffer);
//                    if (!TextUtils.isEmpty(responseInfo)) {
//                        Log.w("RegisterActivity", responseInfo);
//                    }
////                    Thread.sleep(1000);
//                }

            }
            // 关闭网络
//            serverSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
