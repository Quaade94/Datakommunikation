package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import cutter.Cutter;
import cutter.UDPException;

class UDPClient{
	public static void main(String args[]) throws Exception{

		Cutter cutter = new Cutter(1024, 12);
		ArrayList<String> packets = new ArrayList<String>();

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		ArrayList<byte[]> barray = new ArrayList<byte[]>();
		ArrayList<String> received = new ArrayList<String>();

		byte[] receivedData = new byte[1024];
		byte[] sendDataServer = new byte[1024];
		String input;
		String reciept;

		String last = "last";
		byte[] lastPack = new byte[1024];
		lastPack = last.getBytes();

		while(true){

			String sentence = inFromUser.readLine();

			try{
				packets = cutter.messageCutting(sentence);
				for(int i = 0; i < packets.size(); i++){

					byte[] sendData = new byte[1024];
					barray.add(sendData = packets.get(i).getBytes());

				}


				//SENPAI NOTICE ME
				for(int i = 0; i < barray.size(); i++){
					DatagramPacket sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, 9876);
					clientSocket.send(sendPacket);	
				}
				System.out.println("Test");
				DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, 9876);
				clientSocket.send(lastPacket);

				while(true){
					//Receives a motherfucking package from a shitty motherfucker
					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
					clientSocket.receive(receivedPacket);

					//Makes that fucking package into a string... bitch...
					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
					System.out.println("RECEIVED: " + input);
					received.add(input);

					//Get the adress of that motherfucker sending it to ya. He ain't snitching. 
					InetAddress IPAddressServer = receivedPacket.getAddress();
					int port = receivedPacket.getPort();			

					//Sends the receipt to the motherfucker. He should know you mean business...
					reciept = "Your package was recieved: " + input;
					sendDataServer = reciept.getBytes();
					DatagramPacket sendreciept = new DatagramPacket(sendDataServer, sendDataServer.length, IPAddressServer, port);
					clientSocket.send(sendreciept);

					System.out.println(input);
					if(input.equals("last")){
						break;
					}
				}	

				received.remove(received.size()-1);

				String completeMessage = "";
				for(int i = 0; i < received.size(); i++){
					completeMessage = completeMessage + received.get(i);
				}

				System.out.println("FROM SERVER: "+ completeMessage);

			}catch(UDPException e){
				e.myprint();
			}

		}


	}
}