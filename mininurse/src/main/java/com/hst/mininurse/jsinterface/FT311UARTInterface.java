//User must modify the below package with their package name
package com.hst.mininurse.jsinterface;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.widget.Toast;

import com.hst.mininurse.FileUtil;
import com.hst.mininurse.app.MyApplication;
import com.hst.mininurse.ui.MainActivity2;
import com.hst.mininurse.utils.ThreadPool;
import com.hst.mininurse.utils.TypeConversion;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/******************************FT311 GPIO interface class******************************************/
public class FT311UARTInterface  extends Activity{

    private static final String ACTION_USB_PERMISSION = "com.UARTLoopback.USB_PERMISSION";
    public UsbManager usbmanager;
    public UsbAccessory usbaccessory;
    public PendingIntent mPermissionIntent;
    public ParcelFileDescriptor filedescriptor = null;
    public FileInputStream inputstream = null;
    public FileOutputStream outputstream = null;
    public boolean mPermissionRequestPending = false;
    public read_thread readThread;

    private byte[] usbdata;
    private byte[] writeusbdata;
    private byte[] readBuffer; /*circular buffer*/
    private int readcount;
    private int totalBytes;
    private int writeIndex;
    private int readIndex;
    private byte status;
    final int maxnumbytes = 65536;

    public boolean datareceived = false;
    public boolean READ_ENABLE = false;
    public boolean accessory_attached = false;
    public static final String action="android.intent.action.EDIT";
    public Context global_context;

    public static String ManufacturerString = "mManufacturer=FTDI";
    public static String ModelString1 = "mModel=FTDIUARTDemo";
    public static String ModelString2 = "mModel=Android Accessory FT312D";
    public static String VersionString = "mVersion=1.0";

    /*constructor*/
    public FT311UARTInterface(Context context) {
        super();

        global_context = context;
        /*shall we start a thread here or what*/
        usbdata = new byte[2048];
        writeusbdata = new byte[256];
        /*128(make it 256, but looks like bytes should be enough)*/
        readBuffer = new byte[maxnumbytes];


        readIndex = 0;
        writeIndex = 0;
        /***********************USB handling******************************************/

        usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        // Log.d("LED", "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);

        inputstream = null;
        outputstream = null;
    }


    public boolean SetConfig(int baud, byte dataBits, byte stopBits,
                             byte parity, byte flowControl) {

		/*prepare the baud rate buffer*/
        writeusbdata[0] = (byte) baud;
        writeusbdata[1] = (byte) (baud >> 8);
        writeusbdata[2] = (byte) (baud >> 16);
        writeusbdata[3] = (byte) (baud >> 24);

		/*data bits*/
        writeusbdata[4] = dataBits;
        /*stop bits*/
        writeusbdata[5] = stopBits;
        /*parity*/
        writeusbdata[6] = parity;
        /*flow control*/
        writeusbdata[7] = flowControl;

		/*send the UART configuration packet*/
        boolean success = SendPacket((int) 8);
        return success;
    }


    /*write data*/
    public byte SendData(int numBytes, byte[] buffer) {
        FileUtil.write2Filelog("SendData:numbytes:" + numBytes);
        status = 0x00; /*success by default*/
		/*
		 * if num bytes are more than maximum limit
		 */
        if (numBytes < 1) {
			/*return the status with the error in the command*/
            return status;
        }

		/*check for maximum limit*/
        if (numBytes > 256) {
            numBytes = 256;
        }

		/*prepare the packet to be sent*/
        for (int count = 0; count < numBytes; count++) {
            writeusbdata[count] = buffer[count];
        }

        if (numBytes != 64) {
            SendPacket(numBytes);
        } else {
            byte temp = writeusbdata[63];
            SendPacket(63);
            writeusbdata[0] = temp;
            SendPacket(1);
        }

        return status;
    }

    /*read data*/
    public byte ReadData(int numBytes, byte[] buffer, int[] actualNumBytes) {
        status = 0x00; /*success by default*/

		/*should be at least one byte to read*/
        if ((numBytes < 1) || (totalBytes == 0)) {
            actualNumBytes[0] = 0;
            status = 0x01;
            return status;
        }

		/*check for max limit*/
        if (numBytes > totalBytes)
            numBytes = totalBytes;

		/*update the number of bytes available*/
        totalBytes -= numBytes;

        actualNumBytes[0] = numBytes;

		/*copy to the user buffer*/
        for (int count = 0; count < numBytes; count++) {
            buffer[count] = readBuffer[readIndex];
            readIndex++;
			/*shouldnt read more than what is there in the buffer,
			 * 	so no need to check the overflow
			 */
            readIndex %= maxnumbytes;
        }
        return status;
    }

    /*method to send on USB*/
    private boolean SendPacket(int numBytes) {
        FileUtil.write2Filelog("SendPacket:numBytes:" + numBytes);
        try {
            if (outputstream != null) {
                outputstream.write(writeusbdata, 0, numBytes);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public FileOutputStream getOutputStream() {
        return outputstream;
    }

    public FileInputStream closeListenInput() {
        if (readThread != null) {
            readThread.stop();
        }
        return inputstream;
    }

    public void startListenInput() {
        if (readThread != null) {
            readThread.start();
        }
    }

    /*resume accessory*/
    public int ResumeAccessory() {
        // Intent intent = getIntent();
        if (inputstream != null && outputstream != null) {
            return 1;
        }

        UsbAccessory[] accessories = usbmanager.getAccessoryList();
        if (accessories != null) {
            Toast.makeText(global_context, "Accessory Attached", Toast.LENGTH_SHORT).show();
        } else {
            // return 2 for accessory detached case
            //Log.e(">>@@","ResumeAccessory RETURN 2 (accessories == null)");
            accessory_attached = false;
            return 2;
        }

        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (-1 == accessory.toString().indexOf(ManufacturerString)) {
//                Toast.makeText(global_context, "Manufacturer is not matched!", Toast.LENGTH_SHORT).show();
                return 1;
            }

            if (-1 == accessory.toString().indexOf(ModelString1) && -1 == accessory.toString().indexOf(ModelString2)) {
//                Toast.makeText(global_context, "Model is not matched!", Toast.LENGTH_SHORT).show();
                return 1;
            }

            if (-1 == accessory.toString().indexOf(VersionString)) {
//                Toast.makeText(global_context, "Version is not matched!", Toast.LENGTH_SHORT).show();
                return 1;
            }

//            Toast.makeText(global_context, "Manufacturer, Model & Version are matched!", Toast.LENGTH_SHORT).show();
            accessory_attached = true;

            if (usbmanager.hasPermission(accessory)) {
                OpenAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
//                        Toast.makeText(global_context, "Request USB Permission", Toast.LENGTH_SHORT).show();
                        usbmanager.requestPermission(accessory,
                                mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
        }

        return 0;
    }

    /*destroy accessory*/
    public void DestroyAccessory(boolean bConfiged) {

        if (true == bConfiged) {
            READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
            writeusbdata[0] = 0;  // send dummy data for instream.read going
            SendPacket(1);
        } else {
            SetConfig(9600, (byte) 1, (byte) 8, (byte) 0, (byte) 0);  // send default setting data for config
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }

            READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
            writeusbdata[0] = 0;  // send dummy data for instream.read going
            SendPacket(1);
            if (true == accessory_attached) {
            }
//			getApplicationContext().unregisterReceiver(mUsbReceiver);
        }

        try {
            Thread.sleep(10);
        } catch (Exception e) {
        }
        CloseAccessory();
    }

    /*method to send on USB*/
    public void SendPacket(final InputStream inputStream) {

        ThreadPool.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (outputstream != null) {
                        byte buffer[] = new byte[16 * 1024];
                        int temp = 0;
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                        // 循环读取文件
                        while ((temp = bufferedInputStream.read(buffer)) != -1) {
                            // 把数据写入到OuputStream对象中
                            outputstream.write(buffer, 0, temp);
                        }
                        outputstream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*********************helper routines*************************************************/

    public void OpenAccessory(UsbAccessory accessory) {
        filedescriptor = usbmanager.openAccessory(accessory);
        if (filedescriptor != null) {
            usbaccessory = accessory;

            FileDescriptor fd = filedescriptor.getFileDescriptor();

            inputstream = new FileInputStream(fd);
            outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
            if (inputstream == null || outputstream == null) {
                return;
            }

            if (READ_ENABLE == false) {
                READ_ENABLE = true;
                readThread = new read_thread(inputstream);
                readThread.start();
            }
        }
    }

    private void CloseAccessory() {
        try {
            if (filedescriptor != null)
                filedescriptor.close();

        } catch (IOException e) {
        }

        try {
            if (inputstream != null)
                inputstream.close();
        } catch (IOException e) {
        }

        try {
            if (outputstream != null)
                outputstream.close();

        } catch (IOException e) {
        }
		/*FIXME, add the notfication also to close the application*/

        filedescriptor = null;
        inputstream = null;
        outputstream = null;

        System.exit(0);
    }


    /***********USB broadcast receiver*******************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Toast.makeText(global_context, "Allow USB Permission", Toast.LENGTH_SHORT).show();
                        OpenAccessory(accessory);
                    } else {
                        Toast.makeText(global_context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
                        Log.d("LED", "permission denied for accessory " + accessory);

                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                DestroyAccessory(true);
                CloseAccessory();
            } else {
                Log.d("LED", "....");
            }
        }
    };

    public OutputStream sockOutputStream = null;


    public void initRublist() {
        byte buffer[];
        while (true) {
            buffer = new byte[4 * 1024];
            int temp ;
            try {
                temp = inputstream.read(buffer);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            if (temp < 4 * 1024) {
                break;
            }
            buffer = null;
        }
    }

    /*usb input data handler*/
    private class read_thread extends Thread {
        FileInputStream instream;

        read_thread(FileInputStream stream) {
            instream = stream;
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            List<UartData> lists=new ArrayList<UartData>();
            UartData d=new UartData();
            StringBuilder sb=new StringBuilder();
            boolean flag=false;
            boolean stopGet=false;//手机端不在截取数据
            while (READ_ENABLE == true) {
                if (sockOutputStream != null) {
                    if (instream != null) {
                        byte buffer[] = new byte[16 * 1024];
                        int temp = 0;
                        // 循环读取文件
                        try {
                            if ((temp = instream.read(buffer)) != -1) {
                                sockOutputStream.write(buffer, 0, temp);
                                sockOutputStream.flush();
                                //获取串口状态
                                if(!stopGet){
                                    if (flag) {
                                        sb.append(getHexString(buffer, temp));
                                    }
                                    if ("24 24 ".equals(getHexString(buffer, temp))) {
                                        flag = true;
                                        d.setStart("24 24 ");
                                        continue;
                                    }
                                    if ((getHexString(buffer, temp).contains("0A")&&flag)) {
                                        flag = false;
                                        d.setContent(sb.toString());
                                        sb.setLength(0);
                                        d.setEnd("0A ");
                                        lists.add(d);
                                        if (lists.size() == 3) {
                                            Message msg=new Message();
                                            int[] states=new int[2];
                                            String content = lists.get(0).getContent();
                                            FileUtil.write3Filelog(content);
                                            FileUtil.write3Filelog(content.substring(58, 61));
                                            FileUtil.write3Filelog(content.substring(62, 65));
                                            String locationbinary = TypeConversion.hexStr2BinStr(content.substring(58, 61));
                                            String gpsloadbinary = TypeConversion.hexStr2BinStr(content.substring(62, 65));
                                            FileUtil.write3Filelog("locationbinary:"+locationbinary);
                                            FileUtil.write3Filelog("gpsloadbinary:"+gpsloadbinary);
                                            if (locationbinary.charAt(0) == 49) {
                                                FileUtil.write3Filelog("online");
                                                states[0]=1;
                                            } else {
                                                FileUtil.write3Filelog("offline");
                                                states[0]=0;
                                            }
                                            if (gpsloadbinary.charAt(7) == 49) {
                                                FileUtil.write3Filelog("dingwei");
                                                states[1]=1;
                                            } else {
                                                FileUtil.write3Filelog("ximie");
                                                states[1]=0;
                                            }
                                            Intent intent = new Intent(action);
                                            intent.putExtra("data", states);
                                            global_context.sendBroadcast(intent);
                                    //    msg.obj=states;
                                   //     msg.what=6;

                                        //    MainActivity2.instance.getHandler().sendMessage(msg);

                                            for (int i = 3; i > 0; i--) {
                                                lists.remove(i - 1);
                                            }
                                            stopGet=true;
                                        }
                                    }
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    while (totalBytes > (maxnumbytes - 1024)) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        if (instream != null) {
                            readcount = instream.read(usbdata, 0, 2048);
                            if (readcount > 0) {
                                for (int count = 0; count < readcount; count++) {
                                    readBuffer[writeIndex] = usbdata[count];
                                    writeIndex++;
                                    writeIndex %= maxnumbytes;
                                }

                                if (writeIndex >= readIndex)
                                    totalBytes = writeIndex - readIndex;
                                else
                                    totalBytes = (maxnumbytes - readIndex) + writeIndex;

//					    		Log.e(">>@@","totalBytes:"+totalBytes);
                            } else {
                                totalBytes = 0;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
    * 十六进制转二进制
    * */
    public static String hexString2binaryString(String hexString)
    {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++)
        {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }
    class UartData{
        String start;
        String end;
        String content;

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "UartData{" +
                    "start='" + start + '\'' +
                    ", end='" + end + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
    /*
       * 将读取的字节（二进制）数据转化为16进制，以便解析
       * */
    private String getHexString(byte[] data, int nLength) {
        String strTemString = "";
        for (int i = 0; i < nLength; i++) {
            strTemString += String.format("%02X ", data[i]);
        }
        return strTemString;
    }
}