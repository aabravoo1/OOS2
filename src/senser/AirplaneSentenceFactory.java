package senser;

import java.util.ArrayList;

public class AirplaneSentenceFactory {
	
	ArrayList<AircraftSentence> aircraft = new ArrayList<AircraftSentence>();
	
	public ArrayList<AircraftSentence> getArray(String string) {
		//Split the string into array
		String[] split = string.split("\\],\\["); 
		//Convert array into ArrayList
		for(int i = 0; i < split.length; i++) {
			AircraftSentence sentence = new AircraftSentence(split[i]); //create aircraft sentence
			aircraft.add(sentence);//add sentence to arraylist
		}
		
		return this.aircraft;
	}
	
}
