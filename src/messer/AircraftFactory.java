package messer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import senser.AircraftSentence;


public class AircraftFactory {
	
	public BasicAircraft getAircraft(AircraftSentence sentence) {
		String[] data = sentence.getAircraft().split(",");
		Coordinate coords = new Coordinate(Double.parseDouble(data[6]), Double.parseDouble(data[5]));
		/*df = new SimpleDateFormat("ddmmyyyy");*/
		/*SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyyyy");
		Date postime = null;
		try {
			postime = formatter.parse(data[3]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		double speed = 0;
		double trak  = 0;
		try {
			speed = Double.parseDouble(data[9]);
		}catch(NumberFormatException e) {
			if(data[9] == "-0") {
				speed = 0;
			}
		}
		try {
			trak = Double.parseDouble(data[10]);
		}catch(NumberFormatException e) {
			if(data[9] == "-0") {
				trak = 0;
			}
		}
		BasicAircraft aircraftData = new BasicAircraft(data[0], data[1], data[3], coords, speed, trak);//create aircraft sentence

		return aircraftData;
	}
}
