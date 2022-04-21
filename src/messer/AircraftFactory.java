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
		BasicAircraft aircraftData = new BasicAircraft(data[0], data[1], data[3], coords, Double.parseDouble(data[9]), Double.parseDouble(data[10]));//create aircraft sentence

		return aircraftData;
	}
}
