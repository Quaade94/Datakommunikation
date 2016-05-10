package server;

import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;

import cutter.Cutter;
import cutter.ED_Coder;
import cutter.UDPException;

class UDPServer{

	private static DatagramSocket socket;
	private static ArrayList<Long> timeout = new ArrayList<Long>();
	private static ArrayList<Integer> tries = new ArrayList<Integer>();
	private static ArrayList<String> pings = new ArrayList<String>();
	private static String frugt, frugter = "", reciept, completeMessage = "";
	private static int id = 0, port, tryAmount = 5;
	private static InetAddress IPAddress;
	private static long RTT;


	public static void main(String args[]) throws Exception{

		FruitData fruit = new FruitData();
		Cutter cutter = new Cutter(1024, 12);
		ED_Coder coder = new ED_Coder();
		socket = new DatagramSocket(9876);

		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		String serverSYNACK ="SYN+ACK";
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);


		//Three way handshake


		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		socket.receive(receivePacket);		
		String clientSYN = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");
		if(clientSYN.equals("SYN")){
			System.out.println("RECEIVED: "+clientSYN);

			for(int i=0; i<6; i++){
				IPAddress = receivePacket.getAddress();
				port = receivePacket.getPort();
				sendData = serverSYNACK.getBytes();
				DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(sendPacket1);
				System.out.println("SENT: "+serverSYNACK);

				socket.setSoTimeout(2000);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try{
					socket.receive(receivePacket);
				}catch(IOException e){

				}
				String clientACK = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");
				if(clientACK.equals("ACK")){
					System.out.println("RECEIVED: "+clientACK);
					break;
				}
			}
		}

		socket.setSoTimeout(0);

		//ping
		String message = null;
		while(true){

			//recieves ping

			receiveData = new byte[1024];
			DatagramPacket got = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(got);
			message = new String( got.getData(), got.getOffset(), got.getLength(), "UTF-8");
			pings.add(message);
			System.out.println("FROM CLIENT: "+message);
			if(pings.size()>=10)
				break;
		}
		for(int i = 0; i<pings.size();i++){
			message = pings.get(i);
			sendData = new byte[1024];
			sendData = message.getBytes();
			DatagramPacket sendgota = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			socket.send(sendgota);
			System.out.println("TO CLIENT: " + message);
		}
		try{
			receiveData = new byte[1024];
			DatagramPacket got = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(got);
			message = new String( got.getData(), got.getOffset(), got.getLength(), "UTF-8");
			RTT = Long.parseLong(message.substring(4,message.length()));
			System.out.println("RTT was recieved from client: " + RTT + " ms");
		}catch(Exception e){
			System.out.println("Failed to recieve RTT from client!");
		}

		//RECIEVES DATA
		while(true){

			//Receives packages from the client, until package containing the string "last" is received
			receiveData = new byte[1024];
			sendData = new byte[1024];
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivedPacket);


			//Gets the adress of the sender
			IPAddress = receivedPacket.getAddress();
			port = receivedPacket.getPort();			

			//decodes and saves the data to an array
			coder.decode(receivedPacket);

			//sends reciepts for recieved packets
			String forTheReciept = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
			reciept = forTheReciept.substring(0,8);
			sendData = reciept.getBytes();
			DatagramPacket sendreciept = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			socket.send(sendreciept);

			if(!(coder.getMessageArray().contains(null)) && (coder.getMessageArray().get(coder.getMessageArray().size() - 1).equals("last"))){
				//TODO Timeout 
				//last packet is removed because it is just a notice
				coder.getMessageArray().remove(coder.getMessageArray().get(coder.getMessageArray().size()-1));
				//creates the long String
				for(int i = 0; i < coder.getMessageArray().size(); i++){
					completeMessage = completeMessage + coder.getMessageArray().get(i);
				}
				coder.resetArray();
				break;
			}
		}

		//SENDS DATA

		//if the message is "close" shutdown
		if(completeMessage.equals("close")){
			socket.close();
			System.out.println("Shutting down");
			System.exit(0);

		}else{		
			try{
				for(int i = 0; i<completeMessage.length();i++){
					id = Integer.parseInt(completeMessage.substring(i,i+1));
					frugter = frugter + fruit.getFruit(id-1);
				}
			}catch(Exception e){
				//sends error message
				frugt = "You need to enter a valid whole number(Between 1-9)";
				sendData = new byte[1024];
				sendData = frugt.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				System.out.println("TO CLIENT: " + sendData);
				socket.send(sendPacket);
			}

			ArrayList<byte[]> barray = new ArrayList<byte[]>();
			ArrayList<String> packets = new ArrayList<String>();

			//				ArrayList<String> received = new ArrayList<String>();

			//Input from Frugt
			String sentence = frugter;

			try{
				//Cut the message into appropriate sized data amounts
				packets = cutter.messageCutting(sentence);
				packets.add("last");

				//Insert number of packet to the front of each packet e.g. 00000123
				for(int i = 0; i<packets.size();i++){
					packets.set(i, coder.encode(packets.get(i)));
				}

				//Re-writes the messages into a byte arraylist
				for(int i = 0; i < packets.size(); i++){		
					sendData = new byte[1024];
					barray.add(sendData = packets.get(i).getBytes());
				}

				//Creates packages of the bytes and sends them to the receiver
				for(int i = 0; i < barray.size(); i++){
					sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, port);
					socket.send(sendPacket);	
					System.out.println("TO CLIENT: " + packets.get(i));
					//Timer and counter for each package starts here
					timeout.add(System.currentTimeMillis());
					tries.add(1);

				}

			}catch(UDPException e){
				e.myprint();
			}
			String input;

			//puts packet numbers in an array
			ArrayList<String> SPNo = new ArrayList<String>(); //CPNo = Client Package Number
			for(int i=0;i<packets.size();i++){
				SPNo.add(packets.get(i).substring(0, 8));
			}
			//Resending lost packets

			while(true){
				for(int j = 0 ; j < SPNo.size() ; j++){

					// recieves the reciept
					byte[] receivedData = new byte[8];
					DatagramPacket receivedReciept = new DatagramPacket(receivedData, receivedData.length);
					socket.setSoTimeout((int)RTT);
					try{
					socket.receive(receivedReciept);
					}catch(SocketTimeoutException e){
						System.out.println("Failed to recieve reciept");
					}					System.out.println("Reciept recieved from client");
					input = new String( receivedReciept.getData(), receivedReciept.getOffset(), receivedReciept.getLength(), "UTF-8");
					String CPNo = input; //SPNo = Server Package Number
					//handles reciepts to see if all packages were recieved
					for(int i = 0 ; i < SPNo.size() ; i++){
						if(CPNo == SPNo.get(i)){
							SPNo.remove(i);
						}
					}
				}
				if(SPNo.size()==0){

					socket.close();
					System.out.println("the socket was closed");
				}else{
					//for the missing packets:
					for(int i = 0; i < SPNo.size(); i++){
						//find the packet:
						for (int k = 0 ; k<packets.size(); k++){
							if(SPNo.get(i) == packets.get(k).substring(0, 8)){
								//send the packet again:
								sendPacket = new DatagramPacket(barray.get(k), barray.get(k).length, IPAddress, port);
								socket.send(sendPacket);	
								System.out.println("RESENT TO CLIENT: " + packets.get(k));
								//Timer check here
								tries.set(k, (tries.get(k) + 1));
								if(System.currentTimeMillis()-timeout.get(k)> RTT||tries.get(k)>=tryAmount){
									System.out.println("Failed to deliver package no. " + k + " Attempts: " + tries.get(k) + " Timeout: " + timeout.get(k));
									break;
								}
							}
						}
					}
				}
			}
		}
	}
}