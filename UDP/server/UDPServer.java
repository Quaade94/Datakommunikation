package server;

import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;

import cutter.Cutter;
import cutter.ED_Coder;
import cutter.UDPException;

class UDPServer{

	private static DatagramSocket serverSocket;
	private static ArrayList<Long> timeout = new ArrayList<Long>();
	private static ArrayList<Integer> tries = new ArrayList<Integer>();
	private static String frugt, frugter, input, reciept, completeMessage = "";
	private static int id = 0, port, tryAmount = 5;
	private static InetAddress IPAddress;


	public static void main(String args[]) throws Exception{

		FruitData fruit = new FruitData();
		Cutter cutter = new Cutter(1024, 12);
		ED_Coder coder = new ED_Coder();
		serverSocket = new DatagramSocket(9876);
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		String serverSYNACK ="SYN+ACK";
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);


		//Three way handshake


		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);		
		String clientSYN = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");
		if(clientSYN.equals("SYN")){
			System.out.println("RECEIVED: "+clientSYN);

			for(int i=0; i<6; i++){
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
				sendData = serverSYNACK.getBytes();
				DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket1);
				System.out.println("SENT: "+serverSYNACK);

				serverSocket.setSoTimeout(2000);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try{
					serverSocket.receive(receivePacket);
				}catch(IOException e){

				}
				String clientACK = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");
				if(clientACK.equals("ACK")){
					System.out.println("RECEIVED: "+clientACK);
					break;
				}
			}
		}
		serverSocket.setSoTimeout(0);

		//RECIEVES DATA
		while (true){
			while(true){

				//Receives packages from the client, until package containing the string "last" is received
				receiveData = new byte[1024];
				sendData = new byte[1024];
				DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivedPacket);


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
				serverSocket.send(sendreciept);

				if(!(coder.getMessageArray().contains(null)) && (coder.getMessageArray().get(coder.getMessageArray().size()).equals("last"))){
					//TODO Timeout 
					//last packet is removed because it is just a notice
					coder.getMessageArray().remove(coder.getMessageArray().get(coder.getMessageArray().size()));
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
				serverSocket.close();
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
					serverSocket.send(sendPacket);
				}

				ArrayList<byte[]> barray = new ArrayList<byte[]>();
				ArrayList<String> packets = new ArrayList<String>();

				//				ArrayList<String> received = new ArrayList<String>();

				//Input from Frugt
				String sentence = frugter;

				try{
					//Cut the message into appropriate sized data amounts
					packets = cutter.messageCutting(sentence);

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
						System.out.println("TO CLIENT: " + packets.get(i));
						serverSocket.send(sendPacket);	
						//Timer and counter for each package starts here
						timeout.add(System.currentTimeMillis());
						tries.add(1);

					}

					//Creates a last package of bytes, marking to the receiver that this is the last package. 
					String last = "last";
					byte[] lastPack = new byte[1024];
					lastPack = last.getBytes();

					//Sends the last package
					DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, port);
					serverSocket.send(lastPacket);
					System.out.println("TO CLIENT: last");


				}catch(UDPException e){
					e.myprint();

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
							serverSocket.receive(receivedReciept);
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
							serverSocket.close();
						}else{
							//for the missing packets:
							for(int i = 0; i < SPNo.size(); i++){
								//find the packet:
								for (int k = 0 ; k<packets.size(); k++){
									if(SPNo.get(i) == packets.get(k).substring(0, 8)){
										//send the packet again:
										sendPacket = new DatagramPacket(barray.get(k), barray.get(k).length, IPAddress, port);
										System.out.println("RESENT TO SERVER: " + packets.get(k));
										serverSocket.send(sendPacket);	
										//Timer check here
										tries.set(k, (tries.get(k) + 1));
										//TODO
										if(System.currentTimeMillis()-timeout.get(k)> 5||tries.get(k)>=tryAmount){
											System.out.println("Failed to deliver package no. " + k + " Attempts: " + tries.get(k) + " Timeout: " + timeout.get(k));
										break;
										}
									}
								}
							}
						}
					}


					//				I don't know what this kode does ¯\_(ツ)_/¯
					//
					//				packets = cutter.messageCutting(output);
					//				for(int i = 0; i < packets.size(); i++){
					//					sendData = new byte[1024];
					//					barray.add(sendData = packets.get(i).getBytes());
					//				}
					//
					//				//SENPAI NOTICE ME
					//				for(int i = 0; i < barray.size(); i++){
					//					sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, port);
					//					System.out.println("TO CLIENT: " + packets.get(i));
					//					serverSocket.send(sendPacket);	
					//				}
					//
					//				DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, port);
					//				serverSocket.send(lastPacket);
					//				System.out.println("TO CLIENT: last");

					//Receives ack(s) from the fruit
					//				for(int i = 0; i < packets.size()+1; i++){
					//					receiveData = new byte[1024];
					//					//Receives one package from the client
					//					DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
					//					serverSocket.receive(receivedPacket);
					//
					//					//Converts the package into a string
					//					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
					//					System.out.println("FROM CLIENT: " + input);										
					//				}	
				}
			}
			serverSocket.close();;
		}
	}
}