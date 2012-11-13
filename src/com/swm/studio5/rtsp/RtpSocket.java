package com.swm.studio5.rtsp;


import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;



/**
 * RtpSocket implements a RTP socket for receiving and sending RTP packets.
 * <p>
 * RtpSocket is associated to a DatagramSocket that is used to send and/or
 * receive RtpPackets.
 */
public class RtpSocket {
	/** UDP socket */
	SASocket socket;
	DatagramPacket datagram;
	
	/** Remote address */
	InetAddress r_addr;

	/** Remote port */
	int r_port;

	/** Creates a new RTP socket (only receiver) */
	public RtpSocket(SASocket datagram_socket) {
		socket = datagram_socket;
		r_addr = null;
		r_port = 0;
		datagram = new DatagramPacket(new byte[1],1);
	}

	/** Creates a new RTP socket (sender and receiver) */
	public RtpSocket(SASocket datagram_socket,
			InetAddress remote_address, int remote_port) {
		socket = datagram_socket;
		r_addr = remote_address;
		r_port = remote_port;
		datagram = new DatagramPacket(new byte[1],1);
	}

	/** Returns the RTP SipdroidSocket */
	public SASocket getDatagramSocket() {
		return socket;
	}

	/** Receives a RTP packet from this socket */
	public void receive(RtpPacket rtpp) throws IOException {
		datagram.setData(rtpp.packet);
		datagram.setLength(rtpp.packet.length);
		socket.receive(datagram);
		if (!socket.isConnected())
			socket.connect(datagram.getAddress(),datagram.getPort());
		rtpp.packet_len = datagram.getLength();
	}

	/** Sends a RTP packet from this socket */
	public void send(RtpPacket rtpp) throws IOException {
		datagram.setData(rtpp.packet);
		datagram.setLength(rtpp.packet_len);
		datagram.setAddress(r_addr);
		datagram.setPort(r_port);
		socket.send(datagram);
	}

	/** Closes this socket */
	public void close() { // socket.close();
	}

}