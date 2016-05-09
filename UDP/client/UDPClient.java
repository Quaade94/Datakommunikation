package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import cutter.Cutter;
import cutter.ED_Coder;
import cutter.UDPException;

class UDPClient{

	private static String completeMessage = "";
	private static String input;
	private static String reciept;

	public static void main(String args[]) throws Exception{

		//The method for cutting data into packages
		Cutter cutter = new Cutter(1016, 12);
		ED_Coder coder= new ED_Coder();

		//Three way handshake
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		int port = 9876;
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
				return;
			}
		}

		//Testing Ping 10 times
		//Sends a request to the server for and answer and listens, theirfor there is no need for code on the server side.
		long[] ping = new long[10];
		long sum = 0;
		long RTT = 0;
		for(int i= 0; i < 10; i++){
			long time1 = System.currentTimeMillis();
			String message = "Ping "+i+": "+time1;
			DatagramPacket request = new DatagramPacket(message.getBytes(),message.length(),IPAddress,port);
			clientSocket.send(request);
			DatagramPacket reply = new DatagramPacket(new byte[1024],1024);

			//timeout for ping (1 second)
			clientSocket.setSoTimeout(1000);
			try{
				clientSocket.receive(reply);
			}catch(IOException e){

			}
			ping[i] = printData(request,time1);
			//1 second delay for each ping
			Thread.sleep(1000);
		}
		for(int i= 0; i < ping.length; i++){
			sum = sum + ping[i];
		}
		RTT = (sum / ping.length) + 5;

		//Below is the actual program
		byte[] sendDataServer = new byte[1024];

		//SENDS DATA

		ArrayList<String> packets = new ArrayList<String>();
		ArrayList<byte[]> barray = new ArrayList<byte[]>();
		ArrayList<String> received = new ArrayList<String>();

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
				//TODO: Timer starts here

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
								//Timer starter her
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
				clientSocket.close();
			}
		}catch(UDPException e){
			e.myprint();
		}

	}



	//PING METHOD
	//Recieves, works out, and prints the ping
	private static long printData(DatagramPacket request,long time1)throws Exception{
		byte[] buf = request.getData();
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		long time2 = -1;
		try{
			time2  = Long.parseLong(line.substring(8, line.length()));
		}catch(NumberFormatException e){
			System.out.println("Failed to convert String to Long");
			System.out.println("Ping failed!");
		}
		long time = time2-time1;
		System.out.println("Recieved from " + request.getAddress().getHostAddress()+": " + line.substring(0,8)+time+" ms");

		return time;
	}
}