package cutter;

import java.util.ArrayList;

public class CutEncodeDecodeTest {

	public static void main(String[] args) throws Exception {
		
		Cutter cutter = new Cutter(10, 0);
		ED_Coder edCode = new ED_Coder();
		longString message = new longString();
		
		ArrayList<String> messageList = new ArrayList<String>();
		ArrayList<String> encodedmessageList = new ArrayList<String>();

		messageList = cutter.messageCutting(message.getLongString());
				
		for (int i = 0 ; i < messageList.size() ; i++){
			encodedmessageList.add(edCode.encode(messageList.get(i)));
		}
		
		System.out.println("------------------------------");

		for (int i = 0 ; i < messageList.size() ; i++){
			edCode.decodeString(encodedmessageList.get(i));
		}
		
		for (int i = 0 ; i < edCode.getMessageArray().size() ; i++){
			System.out.println(edCode.getMessageArray().get(i));
		}
	}
}
