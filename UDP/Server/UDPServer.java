package Server;

import java.net.*;

class UDPServer{

	private static String output;
	private static String input;
	private static int id;
	private static String reciept;

	public static void main(String args[]) throws Exception{
		FruitData fruit = new FruitData();

		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		byte[] receiveData2 = new byte[1024];
		byte[] sendData2 = new byte[1024];
		while(true){
			//Receives a motherfucking package from the shitty motherfucker
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivedPacket);
			//Makes that fucking package into a string... bitch...
			input = new String( receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");
			System.out.println("RECEIVED: " + input);
			

			//Get the adress of that motherfucker sending it to ya. He ain't snitching. 
			InetAddress IPAddress = receivedPacket.getAddress();
			int port = receivedPacket.getPort();			
			
			//Sends the receipt to the motherfucker. He should know you mean business...
			reciept = "Your package was recieved: " + input;
			sendData = reciept.getBytes();
			DatagramPacket sendreciept = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendreciept);
			
			//That motherfucker called the cops. SHIT. RUN.
			if(input.equals("close")){
				serverSocket.close();
				System.out.println("Shutting down");
				System.exit(0);
			}else{			
			//Get that motherfucker some fruits. Bitches love fruits. 
			id = Integer.parseInt(input.substring(0, 1));
			output = fruit.getFruit(id);
			sendData2 = output.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData2, sendData2.length, IPAddress, port);
			serverSocket.send(sendPacket);
			}
		}
	}
}