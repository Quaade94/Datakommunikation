package Server;

import java.util.ArrayList;

public class Cutter {

	private ArrayList<String> messageCutting(String message, int maxAmountOfPackets) {

		//The size of the message in one packet (1 char = 1 byte)
		int clusterSize = 1024;

		// Index for cutting the message
		int index1 = 0;
		int index2 = clusterSize;

		ArrayList<String> returnMessage = new ArrayList<String>();

		//The remainder of the string when it is cut, if there is 1030 bytes total
		//the remainder will be 1030 mod 1024 = 6, so the last message returned
		//will have a size of 6
		int remainder = message.length() % clusterSize;

		//The amount of packets is to insure that the total size of the message is
		//not too large, and is used to calculate how far the first while loop will run
		int amountOfPackets = message.length()/clusterSize;

		//If the size of the message is too large, this will trigger
		if (amountOfPackets >= maxAmountOfPackets){
			System.out.println("String is " + remainder + " bytes too large");
			return null;
		}

		//Cutting logic
		while (index2 <= (clusterSize * amountOfPackets)){

			//			Adds the cut message, that is, from an index to and index
			returnMessage.add(message.substring(index1, index2));

			index1 = index2;
			index2 = index2 + clusterSize;
		}
		
		//This adds the remaining element to the arrayList 
		if (remainder > 0){
			returnMessage.add(message.substring(index1, index1 + remainder));
		}
		return returnMessage;
	}
}
