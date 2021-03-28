package senser;

import org.json.JSONArray;

import jsonstream.*;

public class Senser implements Runnable
{
	PlaneDataServer server;

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
		String aircraftList;
		JSONArray planeArray;
		
		while (true)
		{
			aircraftList = getSentence();
			System.out.println(aircraftList);
			
			planeArray = server.getPlaneArray();
			System.out.println(planeArray);
		}		
	}
}