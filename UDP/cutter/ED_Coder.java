package cutter;

import java.net.DatagramPacket;
import java.util.ArrayList;

public class ED_Coder {
	
	private int encodeIndex;
	private static String input;
	
	private ArrayList<String> messeges = new ArrayList<String>();
	private ArrayList<Boolean> isThere = new ArrayList<Boolean>();
	@SuppressWarnings("rawtypes")
	private ArrayList<ArrayList> reElement = new ArrayList<ArrayList>();

	
	public ED_Coder(){
		encodeIndex = 1;
		messeges.clear();
		isThere.clear();
		reElement.clear();
	}
		
	/**
	 * resets the index counter back to 1
	 */
	public void resetEncodeIndex(){
		encodeIndex = 1;
	}
	/**
	 * resets the message and isThere arrays, do this when the final message has been constructed
	 */
	public void resetArrays(){
		messeges.clear();
		isThere.clear();
	}
	/**
	 * Adds a number of zero's to the start of the input string
	 * So that it will look like this 00000123RestOfString...
	 * @param message The message that will get an index
	 * @return The message with index
	 */
	public String encode(String message){
		
		if (encodeIndex > 99999999) encodeIndex = 1;
		
		String encodeIndexString = String.valueOf(encodeIndex);

		while (encodeIndexString.length() < 8) {
			encodeIndexString = "0" + encodeIndexString;
		}
		
		encodeIndex++;
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
		
		int elementNumberInt = 1;
		
		for (int i = 0 ; i < 8 ; i++){
			
			String substring = input.substring(i, i);
			
			if(!substring.equals("0")){
				elementNumberInt = i;
			}
		}
		
		String message = input.substring(9, input.length());
		
		messeges.add(elementNumberInt, message);
		isThere.add(elementNumberInt, true);
		
	}
	/**
	 * Gets the messeges and is there Arrays, in a single array
	 * @return An array with messeges array and isThere array
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<ArrayList> getDecodeMessage(){
		
		reElement.clear();
		
		reElement.add(messeges);
		reElement.add(isThere);
		
		return reElement;
	}
}