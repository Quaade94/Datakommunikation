package Client;

import java.io.*;
import java.net.*;

class UDPClient{
	public static void main(String args[]) throws Exception{
		while(true){
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData1 = new byte[1024];
		byte[] receiveData1 = new byte[1024];
		
		byte[] sendData2 = new byte[1024];
		byte[] receiveData2 = new byte[1024];
		
		byte[] sendData3 = new byte[1024];
		byte[] receiveData3 = new byte[1024];
		
		byte[] sendData4 = new byte[1024];
		byte[] receiveData4 = new byte[1024];
		
		byte[] sendData5 = new byte[1024];
		byte[] receiveData5 = new byte[1024];
		
		byte[] sendData6 = new byte[1024];
		byte[] receiveData6 = new byte[1024];
		
		byte[] sendData7 = new byte[1024];
		byte[] receiveData7 = new byte[1024];
		
		byte[] sendData8 = new byte[1024];
		byte[] receiveData8 = new byte[1024];
		
		byte[] sendData9 = new byte[1024];
		byte[] receiveData9 = new byte[1024];
		
		byte[] sendData10 = new byte[1024];
		byte[] receiveData10 = new byte[1024];
		
		byte[] sendData11 = new byte[1024];
		byte[] receiveData11 = new byte[1024];
		
		byte[] sendData12 = new byte[1024];
		byte[] receiveData12 = new byte[1024];
		
		byte[] sendData13 = new byte[1024];
		byte[] receiveData13 = new byte[1024];
		
		while(true){
			
		String sentence = inFromUser.readLine();
					
		sendData1 = sentence.getBytes();
					
		DatagramPacket sendPacket = new DatagramPacket(sendData1, sendData1.length,           IPAddress, 9876);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData1,           receiveData1.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		
		
		System.out.println("FROM SERVER:" + modifiedSentence);
		}

//		clientSocket.close();
		}

	}
}