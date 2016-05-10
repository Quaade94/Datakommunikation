package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import cutter.Cutter;
import cutter.ED_Coder;
import cutter.UDPException;

class UDPClient{

	private static String completeMessage = "", input, reciept;
	private static int port =  9876, tryAmount = 5;
	private static ArrayList<Long> timeout = new ArrayList<Long>();
	private static ArrayList<Integer> tries = new ArrayList<Integer>();

	public static void main(String args[]) throws Exception{

		Cutter cutter = new Cutter(1016, 12);
		ED_Coder coder= new ED_Coder();
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();

		//Three way handshake
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];

		String clientSYN = "SYN";
		String clientACK = "ACK";

		sendData = clientSYN.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);


		for(int i =0; i<6;i++){
			DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPAddress, port);	

			clientSocket.send(sendPacket1);
			System.out.println("SENT TO SERVER: "+clientSYN);

			clientSocket.setSoTimeout(1000);

			DatagramPacket receivePacket = new DatagramPacket(receiveData,           receiveData.length);
			try{
				clientSocket.receive(receivePacket);
			}catch(IOException e){

			}
			String serverSYNACK = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(),"UTF-8");

			if(serverSYNACK.equals("SYN+ACK")){
				System.out.println("RECEIVED FROM SERVER: " + serverSYNACK);
				sendData = clientACK.getBytes();
				DatagramPacket sendPacket2 = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				clientSocket.send(sendPacket2);
				System.out.println("SENT TO SERVER: "+clientACK);
				break;
			}
			if(i == 5){
				System.out.print("Connection timed out");
			}
		}

		//Testing Ping 10 times
		//Sends a request to the server for and answer and listens, therefore there is no need for code on the server side.
		DatagramPacket request;
		String message, line;
		long[] RTTa = new long[10];
		long  time1, time2 = 0, oldRTT, newRTT, RTT = 0, a=5;
		boolean first = false;

		for(int i= 0; i < 10; i++){
			time1 = System.currentTimeMillis();
			message = "Ping "+i+": "+time1;
			request = new DatagramPacket(message.getBytes(),message.length(),IPAddress,port);
			clientSocket.send(request);
			DatagramPacket reply = new DatagramPacket(new byte[1024],1024);

			try{
				clientSocket.receive(reply);
			}catch(IOException e){

			}
			byte[] buf = request.getData();
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			InputStreamReader isr = new InputStreamReader(bais);
			BufferedReader br = new BufferedReader(isr);
			line = br.readLine();
			time2 = System.currentTimeMillis();
			RTTa[i] = time2-time1;
			
			System.out.println("Recieved from " + request.getAddress().getHostAddress()+": " + line.substring(0,8)+(time2-time1)+" ms");
			
			if(first){
				oldRTT = RTTa[i-1];
				newRTT = RTTa[i];
				a = a/10;
			RTT = (a*oldRTT)+((1-a)*newRTT);
			}
			first = true;
		}
		
		//Below is the actual program
		while(true){
			byte[] sendDataServer = new byte[1024];

			//SENDS DATA

			ArrayList<String> packets = new ArrayList<String>();
			ArrayList<byte[]> barray = new ArrayList<byte[]>();

			//Input from user
			String sentence = inFromUser.readLine();

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
					System.out.println("TO SERVER: " + packets.get(i));
					clientSocket.send(sendPacket);	
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
				clientSocket.send(lastPacket);
				System.out.println("TO SERVER: last");

				//RESEND

				//puts packet numbers in an array
				ArrayList<String> CPNo = new ArrayList<String>(); //CPNo = Client Package Number
				for(int i=0;i<packets.size();i++){
					CPNo.add(packets.get(i).substring(0, 8));
				}
				//resending lost packages

				while(true){
					for(int j = 0 ; j < CPNo.size() ; j++){

						// recieves the reciept
						byte[] receivedData = new byte[8];
						DatagramPacket receivedReciept = new DatagramPacket(receivedData, receivedData.length);
						clientSocket.receive(receivedReciept);
						input = new String( receivedReciept.getData(), receivedReciept.getOffset(), receivedReciept.getLength(), "UTF-8");
						String SPNo = input; //SPNo = Server Package Number
						//handles reciepts to see if all packages were recieved
						for(int i = 0 ; i < CPNo.size() ; i++){
							if(SPNo == CPNo.get(i)){
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
									System.out.println("RESENT TO SERVER: " + packets.get(k));
									clientSocket.send(sendPacket);	
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

				//				I have no idea what this kode does?

				//				//Receives packages (in this case the acks) from server (WILL STOP WHEN THE CORRECT AMOUNT OF ACK's HAVE BEEN RECEIVED. LOOK HERE LARS AND SEBBYG. (gensendelse))
				//				for(int i = 0; i < packets.size()+1; i++){
				//					//Receives one package from the server
				//					byte[] receivedData = new byte[1024];
				//					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
				//					clientSocket.receive(receivedPacket);
				//
				//					//Converts the package into a string
				//					input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
				//					System.out.println("FROM SERVER: " + input);
				//					//Tjek om tiden er g�et her
				//				}	
				//
				if (sentence.equals("close")){
					clientSocket.close();
					System.out.println("Shutting Down");
					System.exit(0);
				}else{

					while(true){

						//Receives one package from the server (in this case the fruit response)
						byte[] receivedData = new byte[1024];
						DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
						clientSocket.receive(receivedPacket);

						//Gets the address of sender
						InetAddress IPAddressServer = receivedPacket.getAddress();
						port = receivedPacket.getPort();			

						//decodes and saves the data to an array
						coder.decode(receivedPacket);

						//Receipt
						String forTheReceiept = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
						reciept = forTheReceiept.substring(0,8);
						sendDataServer = reciept.getBytes();
						DatagramPacket sendreciept = new DatagramPacket(sendDataServer, sendDataServer.length, IPAddressServer, port);
						clientSocket.send(sendreciept);

						//Checks if it is receiving the last package
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

					System.out.println("FROM SERVER: "+ completeMessage);
				}
			}catch(UDPException e){
				e.myprint();
			}
			clientSocket.close();
		}
	}
}