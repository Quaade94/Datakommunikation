package cutter;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class ED_Coder {

	private int encodeIndex;
	private static String input;
	
	// Test array for testing if the strings are not in sync
	private int[] tamperdNumbers = {5,0,2,3,4,1};
	private boolean tamperd = false;

	private ArrayList<String> messages = new ArrayList<String>();

	public ED_Coder(){
		encodeIndex = 0;
		messages.clear();
	}

	/**
	 * resets the index counter back to 1
	 */
	public void resetEncodeIndex(){
		encodeIndex = 0;
	}
	/**
	 * resets the message and isThere arrays, do this when the final message has been constructed
	 */
	public void resetArray(){
		messages.clear();
	}
	public ArrayList<String> getMessageArray(){
	
		return messages;
	}
	/**
	 * Adds a number of zero's to the start of the input string
	 * So that it will look like this 00000123RestOfString...
	 * @param message The message that will get an index
	 * @return The message with index
	 */
	public String encode(String message){

		int targetLenght = message.length() + 8;

		if (encodeIndex > 99999999) encodeIndex = 0;

		String encodeIndexString = String.valueOf(encodeIndex);

		if (tamperd){
			encodeIndexString = String.valueOf(tamperdNumbers[encodeIndex - 1]);
		}

		message = encodeIndexString + message;

		while (message.length() < targetLenght) {
			message = "0" + message;
		}

		encodeIndex++;
		System.out.println(message);
		return message;
	}
	/**
	 * Will take a packet and add its content into the message array
	 * onto the correct index in the array, if the string is 00000123RestOfString...
	 * There will be added >RestOfString...< into message array on index 123
	 * @param receivedPacket The packet
	 * @throws Exception  ¯\_(ツ)_/¯
	 */
	public void decode(DatagramPacket receivedPacket) throws Exception {

		input = new String(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength(), "UTF-8");

		int messageNumber = Integer.valueOf(input.substring(0, 8));
		
		String finalSubstring = input.substring(8, input.length());

		messages = this.addIntoArray(messages, finalSubstring, messageNumber);

	}
	/**
	 * Gets the messeges and is there Arrays, in a single array
	 * @return An array with messeges array and isThere array
	 */
	public void decodeString(String message) throws Exception {
		
		int messageNumber = Integer.valueOf(message.substring(0, 8));
				
		String finalSubstring = message.substring(8, message.length());

		messages = this.addIntoArray(messages, finalSubstring, messageNumber);
	}

	private ArrayList<String> addIntoArray(ArrayList<String> list, String message, int placement){
		
		if (list.isEmpty()){
			list.add(0, null);
		}
		for (int i = 0 ; i <= placement ; i++){

			if (i != placement){
				if (i > list.size() - 1){
					list.add(null);
				}
				if (list.get(i) != null && !(list.get(i) instanceof String)){
					String tempString = null;
					
					if (list.get(i)instanceof String && list.get(i) != null){
					tempString = list.get(i);
					}
					list.set(i, null);
					list.set(i, tempString);	
				}
			}
			else {
				if (list.size() != placement){
					if (list.get(placement) == null){
						list.set(placement, message);
					}					
				}
				else{
				list.add(i, message);
				}
			}
		}
		return list;
	}
}