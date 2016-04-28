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
	private static ArrayList<String> received = new ArrayList<String>();
	private static ArrayList<byte[]> barray = new ArrayList<byte[]>();
	private static ArrayList<String> packets = new ArrayList<String>();
	private static byte[] receiveData = new byte[1024];
	private static byte[] sendData = new byte[1024];

	private static DatagramSocket serverSocket;

	public static void main(String args[]) throws Exception{
		FruitData fruit = new FruitData();
		Cutter cutter = new Cutter(1024, 12);
		serverSocket = new DatagramSocket(9876);

		String last = "last";
		byte[] lastPack = new byte[1024];
		lastPack = last.getBytes();

		while(true){

			while(true){
				//Receives a motherfucking package from a shitty motherfucker
				DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivedPacket);

				//Makes that fucking package into a string... bitch...
				input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
				System.out.println("RECEIVED: " + input);
				received.add(input);

				//Get the adress of that motherfucker sending it to ya. He ain't snitching. 
				IPAddress = receivedPacket.getAddress();
				port = receivedPacket.getPort();			

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

			//That motherfucker called the cops. SHIT. RUN.
			if(completeMessage.equals("close")){
				serverSocket.close();
				System.out.println("Shutting down");
				System.exit(0);

			}else{		

				System.out.println(completeMessage);
				try{
					id = Integer.parseInt(completeMessage.substring(0));
					output = fruit.getFruit(id);
				}catch(Exception e){
					output = "You need to enter a valid whole number";
				}

				packets = cutter.messageCutting(output);
				for(int i = 0; i < packets.size(); i++){
					byte[] sendData = new byte[1024];
					barray.add(sendData = packets.get(i).getBytes());
				}

				//SENPAI NOTICE ME
				//TODO fix
				for(int i = 0; i < packets.size(); i++){
					for(int i = 0; i < barray.size(); i++){
						DatagramPacket sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, port);
						serverSocket.send(sendPacket);	
					}
				}

				DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, port);
				serverSocket.send(lastPacket);
			}
		}
	}
}
}