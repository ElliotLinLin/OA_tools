package com.hst.mininurse;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hst.mininurse.jsinterface.FT311UARTInterface;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Ft311URTUtils {

    private FT311UARTInterface uartInterface;
    // 写数据
    byte[] writeBuffer = new byte[64];
    byte status;

    // 读数据
    byte[] readBuffer = new byte[4096];
    int[] actualNumBytes = new int[1];
    char[] readBufferToChar = new char[4096];
    String readSB = "";
    public static boolean isReadDataThreadStart = false;//handlerThread是否启动

    public handler_thread handlerThread;
    int readTimes = 3;
    final int READ_NULL_STATE = 5858;

    public Ft311URTUtils(FT311UARTInterface FT311UARTInterface) {
        uartInterface = FT311UARTInterface;
//		handlerThread = new handler_thread(handler);
//		handlerThread.start();

    }

    private OnUsbDataReceiveListener ReceiveListener;

    /**
     * 接受数据监听
     *
     * @param l
     */
    public void OnUsbDataReceiveListener(OnUsbDataReceiveListener l) {
        this.ReceiveListener = l;
    }

    // 写数据
    public void writeData(String srcStr) {

        if (isOldDevice) {
            if (srcStr.contains("*8888")) srcStr = srcStr.replace("*8888", "D");
        }


        try {
            writeBuffer = srcStr.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (isOldDevice) {//如果是老设备
            byte[] buffer = new byte[writeBuffer.length + 1];
//            buffer[0] = 0x03;
            for (int i = 0; i < writeBuffer.length; i++) {
                buffer[i] = writeBuffer[i];
            }
            buffer[writeBuffer.length] = (byte) 0xFF;
//		FileUtil.write2Filelog("writeData:"+srcStr);
            uartInterface.SendData(buffer.length, buffer);
        } else {
            uartInterface.SendData(writeBuffer.length, writeBuffer);
        }

        readTimes = 3;
        isReadDataThreadStart = true;
//		handlerThread.run();
        handlerThread = new handler_thread(handler);
        handlerThread.start();

    }

    public void writeEmpty() {
        readTimes = 3;
        isReadDataThreadStart = true;
//		handlerThread.run();
        handlerThread = new handler_thread(handler);
        handlerThread.start();
    }
    /**
     * 思路:
     * 1: 当用户点击普通输入数据按钮需要做以下两个步:
     * 		(1): 关闭长连接(同时暂停长连接自动连接)
     * 		(2): 将流通道切换到普通数据
     * 		(3): 界面长连接按钮切换到关闭状态
     *
     * 2: 当用户点击长连接按钮需要做:
     * 		(1): 打开socket(打开长连接)
     * 		(2): 将流通道切换到长连接状态
     */

    /**
     * 停止传统方式从USB读取数据 为了将流给socket做准备
     */
    public FileInputStream stopReadUsbString() {
        if (uartInterface != null)
            return uartInterface.closeListenInput();
        return null;
    }


    public void startReadUsbString() {
        if (uartInterface != null)
            uartInterface.startListenInput();
    }

    public OutputStream getUsbOutPut() {
        if (uartInterface != null)
            return uartInterface.getOutputStream();
        return null;
    }


    /* usb input data handler */
    private class handler_thread extends Thread {
        Handler mHandler;

        /* constructor */
        handler_thread(Handler h) {
            mHandler = h;
        }

        public void run() {
            Message msg;

            while (isReadDataThreadStart) {

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                readTimes--;
                FileUtil.write2Filelog("handler_thread:times:" + readTimes);
                status = uartInterface.ReadData(1024 * 4, readBuffer,
                        actualNumBytes);
                FileUtil.write2Filelog("handler_thread:status:" + status);
                Log.w("text", "readTimes:" + readTimes);
                if (status == 0x00 && actualNumBytes[0] > 0) {
                    msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                    return;
                } else if (readTimes < 0) {//取了3次还没有读到
                    msg = mHandler.obtainMessage();
                    msg.what = READ_NULL_STATE;//读到null
                    mHandler.sendMessage(msg);
                    return;
                }

            }
        }
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("lyq", "handler------");
            FileUtil.write2Filelog("handler");
            if (msg.what == 0) {
                for (int i = 0; i < actualNumBytes[0]; i++) {
                    readBufferToChar[i] = (char) readBuffer[i];
                }
                Log.i("test", "ReceiveListener=msg.what==" + msg.what);

                appendData(readBuffer, actualNumBytes[0]);

            } else if (msg.what == READ_NULL_STATE) {//3次没有读到
//				readTimes=3;
//				isReadDataThreadStart=false;
                appendDataNull();

            }

        }
    };

    public boolean isOldDevice = false;


    public void appendData(byte[] readBufferToChar, int actualNumBytes) {
        Log.i("lyq", "appendData");
        FileUtil.write2Filelog("appendData:actualNumBytes:" + actualNumBytes);
        if (readSB != null) {
            readSB = "";
        }
//		readSB=String.copyValueOf(readBufferToChar, 0, actualNumBytes);
        byte[] temp = new byte[actualNumBytes];
        if (!isOldDevice) {
            System.arraycopy(readBufferToChar, 0, temp, 0, actualNumBytes);
        } else {
            System.arraycopy(readBufferToChar, 1, temp, 0, actualNumBytes - 2);
        }
        try {
            readSB = new String(temp, "GB2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//			FileUtil.write2Filelog("appendData:readSB"+readSB);
        Log.w("text", "readSB" + readSB);
        readTimes = 3;
        isReadDataThreadStart = false;
        if (ReceiveListener != null) {
            ReceiveListener.onUsbDataReceive(readSB);
        }

    }

    public void appendDataNull() {
        FileUtil.write2Filelog("appendDataNull:");
        readSB = "";
        readTimes = 3;
        isReadDataThreadStart = false;
        if (ReceiveListener != null) {
            ReceiveListener.onUsbDataReceive(readSB);
        }

    }
}
