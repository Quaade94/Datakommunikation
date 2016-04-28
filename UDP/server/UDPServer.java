package server;

import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;

import cutter.Cutter;

class UDPServer{

	private static String output;
	private static String input;
	private static int id;
	private static String reciept;
	private static InetAddress IPAddress;
	private static int port;



	private static DatagramSocket serverSocket;

	public static void main(String args[]) throws Exception{
		FruitData fruit = new FruitData();
		Cutter cutter = new Cutter(1024, 12);
		serverSocket = new DatagramSocket(9876);

		String last = "last";
		byte[] lastPack = new byte[1024];
		lastPack = last.getBytes();

		while(true){
			ArrayList<String> received = new ArrayList<String>();
			ArrayList<byte[]> barray = new ArrayList<byte[]>();
			ArrayList<String> packets = new ArrayList<String>();
			
			while(true){
				//Receives packages from the client, until package containing the string "last" is received
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivedPacket);

				//Makes the package into a string
				input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
				System.out.println("FROM CLIENT: " + input);
				received.add(input);

				//Gets the adress of the sender
				IPAddress = receivedPacket.getAddress();
				port = receivedPacket.getPort();			
				
				reciept = "Your package was recieved: " + input;
				sendData = reciept.getBytes();
				DatagramPacket sendreciept = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendreciept);
				
				if(input.equals("last")){
					break;
				}
			}	

			received.remove(received.size()-1);

			String completeMessage = "";
			for(int i = 0; i < received.size(); i++){
				completeMessage = completeMessage + received.get(i);
			}

			//if the message is "close" shutdown
			if(completeMessage.equals("close")){
				serverSocket.close();
				System.out.println("Shutting down");
				System.exit(0);

			}else{		

				try{
					id = 0;
					id = Integer.parseInt(completeMessage.substring(0));
					output = fruit.getFruit(id-1);
				}catch(Exception e){
					output = "You need to enter a valid whole number(Between 1-11)";
					e.printStackTrace();
				}

				packets = cutter.messageCutting(output);
				for(int i = 0; i < packets.size(); i++){
					byte[] sendData = new byte[1024];
					barray.add(sendData = packets.get(i).getBytes());
				}

				//SENPAI NOTICE ME
					for(int i = 0; i < barray.size(); i++){
						DatagramPacket sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, port);
						System.out.println("TO CLIENT: " + packets.get(i));
						serverSocket.send(sendPacket);	
					}
				
				DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, port);
				serverSocket.send(lastPacket);
				System.out.println("TO CLIENT: last");
				
				//Receives ack(s) from the fruit
				for(int i = 0; i < packets.size()+1; i++){
					byte[] receiveData = new byte[1024];
					//Receives one package from the client
					DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivedPacket);
					
					//Converts the package into a string
					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
					System.out.println("FROM CLIENT: " + input);										
				}	
			}
		}
	
}
}