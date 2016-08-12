package com.hantek.ttia.comms;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;

public class TcpSocketClient implements Runnable, PhysicalInterface {
	private Thread readThread = null;
	private Socket socket = null;
	private OutputStream out;
	private InputStream in;

	private boolean isStart = false;
	private boolean isConnected = false;
	private String ip = "";
	private int port = 0;
	private DataListener dataRecvInterface;

	private SocketState state = SocketState.Unknown;

	public TcpSocketClient() {
		this.isStart = false;
		this.isConnected = false;
		this.state = SocketState.Unknown;
	}

	/**
	 * options=ip,port [127.0.0.0,80]
	 */
	@Override
	public boolean open(String options) {
		if (this.isStart) {
			return false;
		}

		this.isStart = true;
		String[] data = options.split(",");
		this.ip = data[0];
		this.port = Integer.parseInt(data[1]);

		/* Create a receiving thread */
		this.readThread = new Thread(this);
		this.readThread.setName("TcpClient");
		this.readThread.start();
		return true;
	}

	@Override
	public boolean close() {
		if (!this.isStart) {
			return false;
		}

		this.isStart = false;
		if (this.readThread != null) {
			try {
				this.readThread.join(1000);
			} catch (InterruptedException e) {
				this.readThread.interrupt();
			}
		}

		return true;
	}

	@Override
	public boolean isOpen() {
		return this.isStart;
	}

	@Override
	public Calendar getLastReceiveTime() {
		return Calendar.getInstance();
	}

	@Override
	public boolean send(final byte[] dataByte) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				callSend(dataByte);
			}

		}).start();

		return true;
	}

	@Override
	public void setListener(DataListener listener) {
		this.dataRecvInterface = listener;
	}

	@Override
	public void run() {
		while (this.isStart) {

			// do connect
			Obs(SocketState.Connecting);
			while (this.isStart) {
				try {
					Thread.sleep(50);
					socket = new Socket();
					socket.setKeepAlive(true);
					socket.connect(new InetSocketAddress(InetAddress.getByName(this.ip), this.port), 3000);
					this.isConnected = true;
					out = socket.getOutputStream();
					break;
				} catch (Exception e) {

				}
			}

			// do receive
			Obs(SocketState.Connected);
			
			byte[] buffer;
			while (this.isStart && this.isConnected) {
				try {
					Thread.sleep(50);
					in = socket.getInputStream();
					int count = in.available();
					if (count > 0) {
						buffer = new byte[count];
						in.read(buffer);

						Obs(buffer);
					}
				} catch (Exception e) {
					this.isConnected = false;
					break;
				}
			}

			this.isConnected = false;
			Obs(SocketState.Disconnected);
		}
	}

	public SocketState getStatus() {
		return this.state;
	}

	private boolean callSend(byte[] dataByte) {
		try {
			out.write(dataByte);
			out.flush();
		} catch (Exception e) {
			isConnected = false;
		}
		return true;
	}

	private void Obs(byte[] rawData) {
		if (this.dataRecvInterface != null)
			this.dataRecvInterface.onDataReceived(rawData, rawData.length);
	}

	private void Obs(SocketState state) {
		System.out.println("Tcp state " + state);
		// this.state = state;
		// if (_listeners != null && _listeners.isEmpty() == false) {
		// Enumeration<DataReceivedInterface> e = _listeners.elements();
		// while (e.hasMoreElements()) {
		// DataReceivedInterface listener = e.nextElement();
		// listener.onStateChanged(state);
		// }
		// }
	}
}
