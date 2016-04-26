package Server;

import java.io.*;
import java.net.*;

class UDPServer{

	private static String output;
	private static String input;
	private static int id;
	private static String reciept;

	public static void main(String args[]) throws Exception{
		FruitData fruit = new FruitData();

		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while(true){
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			input = new String( receivePacket.getData());
			id = Integer.parseInt(input.substring(0, 1));
			System.out.println("RECEIVED: " + input);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			reciept = "Your package was recieved";
			sendData = reciept.getBytes();
			DatagramPacket sendreciept = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendreciept);
			output = fruit.getFruit(id);

			sendData = output.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}
	}
}