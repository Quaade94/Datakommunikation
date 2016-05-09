package server;

import java.util.ArrayList;

public class FruitData {
	ArrayList<String> fruitArray = new ArrayList<String>();

	public FruitData(){
		this.fruitArray.add("Æble ");
		this.fruitArray.add("Avokado ");
		this.fruitArray.add("Hindbær ");
		this.fruitArray.add("Blomme ");
		this.fruitArray.add("Banan ");
		this.fruitArray.add("Blåbær ");
		this.fruitArray.add("Vandmelon ");
		this.fruitArray.add("Jordbær ");
		this.fruitArray.add("Brombær ");
		this.fruitArray.add("Citron ");
		this.fruitArray.add("Honningmelon ");
	}

	public String getFruit(int ID){
		return fruitArray.get(ID);
	}
}
