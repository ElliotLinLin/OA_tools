package com.hst.mininurse;

import android.os.SystemClock;
import android.util.Log;

import com.hst.mininurse.utils.ThreadPool;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/9/30
 */

public class SocketUtil {

    public String IP;
    public int PORT;
    private Socket mSocket;
    private boolean isOpenLongLink = false;//是否开启断线重连
    private int longLinkPeriod = 10;//断线重连周期单位是秒
    private boolean isConnecting = false;//是否正在连接socket

    /**
     * 通过发送心跳包检查socket是否正在连接
     *
     * @return
     */
    public boolean setHearData() {
        if (mSocket == null) return false;

        try {
            mSocket.sendUrgentData(0xFF);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (!startPing()) return false;
        return true;

    }

    private boolean startPing() {
        Log.e("Ping", "startPing...");
        boolean success = false;
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 2 " + IP);
            int status = p.waitFor();
            if (status == 0) {
                success = true;
            } else {
                success = false;
            }
        } catch (IOException e) {
            success = false;
        } catch (InterruptedException e) {
            success = false;
        } finally {
            p.destroy();
        }

        return success;
    }


    /**
     * 开启自动重连
     */
    public void openLongLink() {
        isOpenLongLink = true;
        if (isConnecting) return;
        ThreadPool.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (isOpenLongLink) {
                    boolean b = setHearData();
                    Log.w("SocketUtil", "b:" + b);
                    boolean isConnecting = SocketUtil.this.isConnecting;
                    if (!b && !isConnecting) {//发送心跳包  如果断开连接了  就连接
                        try {
                            if (isConnecting) return;
                            openSocket();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    SystemClock.sleep(1000 * longLinkPeriod);
                }
            }
        });
    }

    public void closeLongLink() {
        isOpenLongLink = false;
    }

    public void closeSocket() {
        closeLongLink();
        try {
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeStream(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 得到socket 并且设置IP 端口
     *
     * @param ip
     * @param port
     * @return
     */
    public void getSocket(String ip, int port, boolean openLongLink, OnOpenSocketSuccess openSocketSuccess) {
        this.IP = ip;
        this.PORT = port;
        this.isOpenLongLink = openLongLink;
        this.mOpenSocketSuccess = openSocketSuccess;
        if (mSocket == null) { //如果socket从来没创建  就连接
            openSocket();
        } else if (!setHearData()) {//如果socket创建过  但是断开了  就重新连接
            openSocket();
        }
        if (openLongLink) openLongLink();
    }


    public interface OnOpenSocketSuccess {
        void onOpenSocketSuccess(Socket socket);

        void onOpenSocketFail(Exception e);
    }

    private OnOpenSocketSuccess mOpenSocketSuccess;

    public void setOpenSocketSuccess(OnOpenSocketSuccess openSocketSuccess) {
        mOpenSocketSuccess = openSocketSuccess;
    }

    /**
     * 开启socket 必须在子线程执行
     */
    public void openSocket() {
        if (isConnecting) return;
        ThreadPool.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    isConnecting = true;
                    mSocket = new Socket(IP, PORT);
                    // 客户端socket在接收数据时，有两种超时：1. 连接服务器超时，即连接超时；2. 连接服务器成功后，接收服务器数据超时，即接收超时
                    // 设置 socket 读取数据流的超时时间
//                    clientSocket.setSoTimeout(5000);
                    // 发送数据包，默认为 false，即客户端发送数据采用 Nagle 算法；
                    // 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
                    mSocket.setTcpNoDelay(true);
                    // 设置客户端 socket 关闭时，close() 方法起作用时延迟 30 秒关闭，如果 30 秒内尽量将未发送的数据包发送出去
                    mSocket.setSoLinger(true, 30000);
                    // 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
                    mSocket.setSendBufferSize(4096);
                    // 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
                    mSocket.setReceiveBufferSize(4096);
                    // 作用：每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
                    // 防止服务器端无效时，客户端长时间处于连接状态
                    mSocket.setKeepAlive(true);
                    isConnecting = false;
                    if (mOpenSocketSuccess != null) mOpenSocketSuccess.onOpenSocketSuccess(mSocket);
                } catch (Exception e) {
                    isConnecting = false;
                    e.printStackTrace();
                    if (mOpenSocketSuccess != null) mOpenSocketSuccess.onOpenSocketFail(e);
                }
            }
        });
    }


}
