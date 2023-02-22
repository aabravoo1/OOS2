package senser;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.regex.Matcher;
//hello
import org.json.JSONArray;

import jsonstream.*;
import observer.SimpleObservable;

public class Senser extends SimpleObservable<AircraftSentence> implements Runnable 
{
	PlaneDataServer server;
	//Pattern sentence = Pattern.compile("\\[(.*?)\\],");

	public Senser(PlaneDataServer server)
	{
		this.server = server;
	}

	private String getSentence()
	{
		String list = server.getPlaneListAsString();
		return list;
	}
	
	
	public void run()
	{
		ArrayList<AircraftSentence> aircraftList;
		//JSONArray planeArray;
		AirplaneSentenceFactory factory = new AirplaneSentenceFactory();
		AircraftDisplay display = new AircraftDisplay();
		
		while (true)
		{
			//get array list from AircraftSentence factory
			aircraftList = factory.getArray(getSentence()); 
			//iterate through arraylist
			//System.out.println("Current aircraft in range: " + aircraftList.size() + "\n");
			for(int i = 0; i < aircraftList.size(); i++) {
				//display.display(aircraftList.get(i));
				setChanged();
				notifyObservers(aircraftList.get(i));
			}
			
			
			//planeArray = server.getPlaneArray();
			//System.out.println(planeArray);
		}		
	}
}