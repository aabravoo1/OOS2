package senser;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
//hello
import org.json.JSONArray;

import jsonstream.*;

public class Senser implements Runnable
{
	PlaneDataServer server;
	Pattern sentence = Pattern.compile("\\[(.*?)\\],");

	public Senser(PlaneDataServer server)
	{
		this.server = server;
	}

	private String getSentence()
	{
		String list = server.getPlaneListAsString();
		return list;
	}
	
	private void observeSentences() {
		String list = getSentence();
		Matcher m = sentence.matcher(list);
		while(m.find()) {
			System.out.println(m.group());
		}
	}
	
	public void run()
	{
		//String[] aircraftList;
		//JSONArray planeArray;
		
		while (true)
		{
			observeSentences();
			
			
			//planeArray = server.getPlaneArray();
			//System.out.println(planeArray);
		}		
	}
}