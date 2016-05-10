package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import cutter.Cutter;
import cutter.ED_Coder;
import cutter.UDPException;

class UDPClient{

	private static String completeMessage = "", input, reciept, clientSYN, clientACK, sentence, message, serverSYNACK, SPNo;
	private static int port =  9876, tryAmount = 5;
	private static ArrayList<Long> timeout = new ArrayList<Long>();
	private static ArrayList<Long> RTTA = new ArrayList<Long>();
	private static ArrayList<Integer> tries = new ArrayList<Integer>();
	private static ArrayList<String> packets = new ArrayList<String>();
	private static ArrayList<byte[]> barray = new ArrayList<byte[]>();
	private static long  time1 = 0, time2 = 0, oldRTT, newRTT, RTT = 0, a=5;
	private static BufferedReader inFromUser;
	private static DatagramSocket socket;
	private static ED_Coder coder;
	private static Cutter cutter;
	private static byte[] sendData;
	private static byte[] receiveData;
	private static InetAddress IPAddress;
	private static byte[] receivedData = new byte[1024];
	private static ArrayList<String> CPNo = new ArrayList<String>();		//CPNo = Client Package Number
	private static DatagramPacket sendPacket, receivePacket, receivedReciept;

	public static void main(String args[]) throws Exception{

		cutter = new Cutter(1016, 12);
		coder = new ED_Coder();
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		socket = new DatagramSocket();

		//Three way handshake
		IPAddress = InetAddress.getByName("localhost");
		sendData = new byte[1024];
		receiveData = new byte[1024];

		clientSYN = "SYN";
		clientACK = "ACK";

		sendData = clientSYN.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);


		for(int i =0; i<6;i++){
			DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPAddress, port);	

			socket.send(sendPacket1);
			System.out.println("SENT TO SERVER: "+clientSYN);

			socket.setSoTimeout(1000);

			receivePacket = new DatagramPacket(receiveData,           receiveData.length);
			try{
				socket.receive(receivePacket);
			}catch(IOException e){

			}
			serverSYNACK = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");

			if(serverSYNACK.equals("SYN+ACK")){
				System.out.println("RECEIVED FROM SERVER: " + serverSYNACK);
				sendData = clientACK.getBytes();
				DatagramPacket sendPacket2 = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(sendPacket2);
				System.out.println("SENT TO SERVER: "+clientACK);
				break;
			}
			if(i == 5){
				System.out.print("Connection timed out");
			}
		}
		//Testing Ping 10 times

		for(int i= 0; i < 10; i++){
			time1 = System.currentTimeMillis();
			String message = "Ping "+i+": "+time1;

			//sends ping
			sendData = new byte[1024];
			sendData = message.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			socket.send(sendPacket);
			System.out.println("TO SERVER: " + message);
		}

		while(RTTA.size()<10){

			//recieves ping
			receiveData = new byte[1024];
			DatagramPacket got = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(got);
			message = new String( got.getData(), got.getOffset(), got.getLength(), "UTF-8");
			System.out.println("FROM SERVER: "+message);
			time2 = System.currentTimeMillis();

			RTTA.add(time2-Long.parseLong(message.substring(8,message.length())));
		}
		for(int i = 1; i<RTTA.size(); i++){
			oldRTT = RTTA.get(i-1);
			newRTT = RTTA.get(i);
			a = a/10;
			RTT = (a*oldRTT)+((1-a)*newRTT);
			RTT = RTT *(long)1.1;
			System.out.println("Round trip time: " + RTT + " ms");
		}
		String message = "RTT="+RTT;
		sendData = new byte[1024];
		sendData = message.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		socket.send(sendPacket);
		System.out.println("TO SERVER: " + message);

		//Below is the actual program
		while(true){

			//SENDS DATA

			//Input from user
			sentence = inFromUser.readLine();

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
					System.out.println("TO SERVER: " + packets.get(i));
					//Timer and counter for each package starts here
					timeout.add(System.currentTimeMillis());
					tries.add(1);
				}

				//RESEND
				
				//puts packet numbers in an array
				for(int i=0;i<packets.size();i++){
					CPNo.add(packets.get(i).substring(0, 8));
				}
				//resending lost packages

				while(true){
					//Thread.sleep(RTT);
					for(int j = 0 ; j < CPNo.size() ; j++){
						// recieves the reciept
						byte[] receivedData = new byte[8];
						receivedReciept = new DatagramPacket(receivedData, receivedData.length);
						socket.receive(receivedReciept);
						System.out.println("Reciept recieved from server");
						input = new String( receivedReciept.getData(), receivedReciept.getOffset(), receivedReciept.getLength(), "UTF-8");
						SPNo = input; //SPNo = Server Package Number
						//handles reciepts to see if all packages were recieved
						for(int i = 0 ; i < CPNo.size() ; i++){
							if(SPNo.equals(CPNo.get(i))){
								CPNo.remove(i);
							}
						}
					}
					if(CPNo.size()==0){
						break;						
					}else{
						//for the missing packets:
						for(int i = 0; i < CPNo.size(); i++){
							//find the packet:
							for (int k = 0 ; k<packets.size(); k++){
								if(CPNo.get(i) == packets.get(k).substring(0, 8)){
									//send the packet again:
									sendPacket = new DatagramPacket(barray.get(k), barray.get(k).length, IPAddress, port);
									socket.send(sendPacket);	
									System.out.println("RESENT TO SERVER: " + packets.get(k));
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
				
				//RECIEVE DATA
				if (sentence.equals("close")){
					socket.close();
					System.out.println("Shutting Down");
					System.exit(0);
				}else{

					while(true){

						//Receives one package from the server (in this case the fruit response)
						DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
						socket.receive(receivedPacket);

						//Gets the address of sender
						InetAddress IPAddressServer = receivedPacket.getAddress();
						port = receivedPacket.getPort();			

						//decodes and saves the data to an array
						coder.decode(receivedPacket);

						//Receipt
						String forTheReceiept = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
						reciept = forTheReceiept.substring(0,8);
						sendData = new byte[8];
						sendData = reciept.getBytes();
						DatagramPacket sendreciept = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
						socket.send(sendreciept);
						System.out.println("Reciept sent to Server");

						//Checks if it is receiving the last package
						if(!(coder.getMessageArray().contains(null)) && (coder.getMessageArray().get(coder.getMessageArray().size()-1).equals("last"))){
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
					System.out.println("FROM SERVER: "+ completeMessage);
				}
			}catch(UDPException e){
				e.myprint();
			}
		}
	}
}