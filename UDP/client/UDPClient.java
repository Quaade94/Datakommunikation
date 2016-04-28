package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import cutter.Cutter;
import cutter.UDPException;

class UDPClient{
	public static void main(String args[]) throws Exception{

		//The method for cutting data into packages
		Cutter cutter = new Cutter(1024, 12);

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		String clientSYN = "SYN";
		String clientACK = "ACK";
		
		
			
		sendData = clientSYN.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,           IPAddress, 9876);
		clientSocket.send(sendPacket);
		System.out.println("SENT TO SERVER: "+clientSYN);
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData,           receiveData.length);
		clientSocket.receive(receivePacket);
		String serverSYNACK = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");
		System.out.println("RECEIVED FROM SERVER: " + serverSYNACK);
		
		
		
		sendData = clientACK.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
		clientSocket.send(sendPacket);
		System.out.println("SENT TO SERVER: "+clientACK);
		
		
		byte[] sendDataServer = new byte[1024];
		
		String input;
		String reciept;

		//Creates a last package of bytes, marking to the receiver that this is the last package. 
		String last = "last";
		byte[] lastPack = new byte[1024];
		lastPack = last.getBytes();

		while(true){

			ArrayList<String> packets = new ArrayList<String>();
			ArrayList<byte[]> barray = new ArrayList<byte[]>();
			ArrayList<String> received = new ArrayList<String>();
			
			//Input from user
			String sentence = inFromUser.readLine();

			try{
				//Cut the message into appropriate sized dataamounts
				packets = cutter.messageCutting(sentence);
				
				//Re-writes the messages into a byte arraylist
				for(int i = 0; i < packets.size(); i++){		
					sendData = new byte[1024];
					barray.add(sendData = packets.get(i).getBytes());
				}

				//Creates packages of the bytes and sends them to the receiver
				for(int i = 0; i < barray.size(); i++){
					sendPacket = new DatagramPacket(barray.get(i), barray.get(i).length, IPAddress, 9876);
					System.out.println("TO SERVER: " + packets.get(i));
					clientSocket.send(sendPacket);	
				}
				
				//Sends the last package
				DatagramPacket lastPacket = new DatagramPacket (lastPack, lastPack.length, IPAddress, 9876);
				clientSocket.send(lastPacket);
				System.out.println("TO SERVER: last");
			
				//Receives packages (in this case the acks) from server (WILL STOP WHEN THE CORRECT AMOUNT OF ACK's HAVE BEEN RECEIVED. LOOK HERE LARS AND SEBBYG. (gensendelse))
				for(int i = 0; i < packets.size()+1; i++){
					//Receives one package from the server
					byte[] receivedData = new byte[1024];
					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
					clientSocket.receive(receivedPacket);

					//Converts the package into a string
					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
					System.out.println("FROM SERVER: " + input);			
				}	
					
				if (sentence.equals("close")){
					clientSocket.close();
					System.out.println("Shutting Down");
					System.exit(0);
				}
				
				while(true){
					//Receives one package from the server (in this case the fruit response)
					byte[] receivedData = new byte[1024];
					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
					clientSocket.receive(receivedPacket);

					//Converts the package into a string
					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
					received.add(input);
					
					//Gets the address of sender
					InetAddress IPAddressServer = receivedPacket.getAddress();
					int port = receivedPacket.getPort();			

					//Receipt
					reciept = "Your package was recieved: " + input;
					sendDataServer = reciept.getBytes();
					DatagramPacket sendreciept = new DatagramPacket(sendDataServer, sendDataServer.length, IPAddressServer, port);
					clientSocket.send(sendreciept);

					//Checks if it is receiving the last package
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