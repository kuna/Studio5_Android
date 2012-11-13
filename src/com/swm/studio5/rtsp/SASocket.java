package com.swm.studio5.rtsp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.net.UnknownHostException;

import com.swm.studio5.rtsp.OSNetworkSystem;
import com.swm.studio5.rtsp.PlainDatagramSocketImpl;

public class SASocket extends DatagramSocket {

	PlainDatagramSocketImpl impl;
	public static boolean loaded = false;
	
	public SASocket(int port) throws SocketException, UnknownHostException {
		super(!loaded?port:0);
		if (loaded) {
			impl = new PlainDatagramSocketImpl();
			impl.create();
			impl.bind(port,InetAddress.getByName("0"));
		}
	}
	
	public void close() {
		super.close();
		if (loaded) impl.close();
	}
	
	public void setSoTimeout(int val) throws SocketException {
		if (loaded) impl.setOption(SocketOptions.SO_TIMEOUT, val);
		else super.setSoTimeout(val);
	}
	
	public void receive(DatagramPacket pack) throws IOException {
		if (loaded) impl.receive(pack);
		else super.receive(pack);
	}
	
	public void send(DatagramPacket pack) throws IOException {
		if (loaded) impl.send(pack);
		else super.send(pack);
	}
	
	public boolean isConnected() {
		if (loaded) return true;
		else return super.isConnected();
	}
	
	public void disconnect() {
		if (!loaded) super.disconnect();
	}
	
	public void connect(InetAddress addr,int port) {
		if (!loaded) super.connect(addr,port);
	}

	static {
			try {
		        System.loadLibrary("OSNetworkSystem");
		        OSNetworkSystem.getOSNetworkSystem().oneTimeInitialization(true);
		        SASocket.loaded = true;
			} catch (Throwable e) {
			}
	}
}