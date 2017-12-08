package com.hst.mininurse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

import android.util.Log;

/**
 * @author Ljj
 *
 * desc:TCP连接服务
 */
public class TCPService {
	protected static final String TAG = "TCPService";
	public static final int STATE_CONNCTING = 1;// 连接中
	public static final int STATE_CONNCTED = 2;// 连接上
	public static final int STATE_DISCONNCTED = 3;// 连接断开
	public static final int STATE_STOP = 4;// 主动关闭
	private int connState = STATE_DISCONNCTED;//当前连接状态

	private OnDataReceiverListener mOnDataReceiverListener;// 数据接收监听
	private OnConnStateChangeListener mOnConnStateChangeListener;// 状态改变监听
	private InputStream mInputStream = null;
	private OutputStream mOutputStream = null;
	private Thread connThread;// 连接线程
	private ServerSocket server = null;//socket服务

	private TCPService() {
	}

	private static class Holder {
		private static TCPService INSTANCE = new TCPService();
	}

	public static TCPService getInstance() {
		return Holder.INSTANCE;
	}

	/**接受数据监听
	 * @param l
	 */
	public void setOnReceiveListener(OnDataReceiverListener l) {
		this.mOnDataReceiverListener = l;
	}

	/**状态改变监听 
	 * @see TCPService.STATE_CONNCTING,</br>TCPService.STATE_CONNCTED,</br>TCPService.STATE_DISCONNCTED,</br>TCPService.STATE_STOP
	 * @param l
	 */
	public void setOnConnStateChangeListener(OnConnStateChangeListener l) {
		this.mOnConnStateChangeListener = l;
	}

	/**发送数据
	 * @param data
	 */
	public void sendData(byte[] data) {
		if (mOutputStream != null && connState == STATE_CONNCTED) {
			try {
				mOutputStream.write(data);
				//				return true;
			} catch (Exception e) {
				e.printStackTrace();
				changeState(STATE_DISCONNCTED);
				mOutputStream = null;
			}
		}
		//		return false;
	}

	/**
	 * 开启监听指定端口
	 * @param port
	 */
	public void start(final int port) {
		if (connState == STATE_CONNCTING || connState == STATE_CONNCTED) {
			return;
		}
		changeState(STATE_CONNCTING);// 连接中
		connThread = new Thread() {
			@Override
			public void run() {
				super.run();
				if (this.isInterrupted()) {
					return;
				}
				try {
					server = new ServerSocket(port);
					while (true) {// 循环接受客户端
						if (this.isInterrupted()) {
							break;
						}
						if (connState != STATE_CONNCTED) {
							Socket client = server.accept();
							Log.d(TAG, "建立连接");
							mInputStream = client.getInputStream();
							mOutputStream = client.getOutputStream();
							int b = -1;
							byte[] buffer = new byte[1024];
							changeState(STATE_CONNCTED);// 连接成功
							Log.d(TAG, "建立成功");
							while (true) {// 循环接收数据
								if (this.isInterrupted()) {
									return;
								}
								if (mInputStream != null) {
									b = mInputStream.read(buffer);
									if (b != -1) {
										if (mOnDataReceiverListener != null) {
											byte[] temp = new byte[b];
											System.arraycopy(buffer, 0, temp, 0, b);
											mOnDataReceiverListener.onReceive(temp);
										}
										continue;
									}
								}
								break;
							}
						} else {
							if (connState == STATE_STOP) {
								break;
							}
							Thread.sleep(500);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					changeState(STATE_DISCONNCTED);// 连接断开
					closeSocket(server);
				}
			}
		};
		connThread.start();
	}
	/**
	 * 关闭服务
	 */
	public void close() {
		try {
			if (connThread != null) {
				connThread.interrupt();
				connThread = null;
			}
			if (server != null) {
				closeSocket(server);
				server = null;
			}
			changeState(STATE_STOP);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	private void changeState(int state) {
		connState = state;
		if (mOnConnStateChangeListener != null) {
			mOnConnStateChangeListener.onChange(connState);
		}
	}

	private void closeSocket(ServerSocket socket) {
		Log.d(TAG, "closeSocket");
		try {
			if (mInputStream != null) {
				mInputStream.close();
			}
			if (mOutputStream != null) {
				mInputStream.close();
			}
			if (socket != null) {
				ServerSocketChannel channel = socket.getChannel();
				socket.close();
				if (channel != null) {
					channel.close();
				}
			}
			Log.d(TAG, "closeSocket-all");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "closeSocket-e-" + e.getMessage());
		}
		// 关闭socket
	}
	public int getConnectState(){
		return connState;
	}
}
