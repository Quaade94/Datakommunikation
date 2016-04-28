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
		
		sentence.getBytes();
							
		sendData1 = sentence.getBytes();
		
		//Sending DATA
		DatagramPacket sendPacket = new DatagramPacket(sendData1, sendData1.length,           IPAddress, 9876);
		clientSocket.send(sendPacket);
		
		//Getting ACK (acknowledgement)
		DatagramPacket ACK = new DatagramPacket(receiveData1, receiveData1.length);
		clientSocket.receive(ACK);
		System.out.println(ACK.getOffset());
		System.out.println(ACK.getLength());
		System.out.println(ACK.getData());
		System.out.println(ACK);
		
		//Printing the ACK message
		String ACKstring = new String(ACK.getData(), ACK.getOffset(), ACK.getLength(), "UTF-8");
		System.out.println("FROM SERVER:" + ACKstring);
		
		if(sentence.equals("close")){
			clientSocket.close();
			System.out.println("Shutting down");
			System.exit(0);
		}
		
		//Receiving the actual data (the fruit)
		DatagramPacket receivedPacket = new DatagramPacket(receiveData2, receiveData2.length);
		clientSocket.receive(receivedPacket);
		
		System.out.println(receivedPacket.getOffset());
		System.out.println(receivedPacket.getLength());
		System.out.println(receivedPacket.getData());
		System.out.println(receivedPacket);
		
		//Printing the fruit
		String FruitString = new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
		System.out.println("FROM SERVER:" + FruitString);
		
		
		}

//		clientSocket.close();
		}

	}
}