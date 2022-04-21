package messer;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jsonstream.PlaneDataServer;
import observer.Observable;
import observer.Observer;
import senser.AircraftSentence;


public class Messer implements Observer<AircraftSentence>, Runnable{
	ArrayBlockingQueue<AircraftSentence> buffer;
	
	//LINKED BLOCKING QUEUE
	
	public Messer() {
		buffer = new ArrayBlockingQueue<AircraftSentence>(20);
	}
	
	@Override
	public void update(Observable<AircraftSentence> observable, AircraftSentence newValue) {
		// TODO Auto-generated method stub
		try {
			buffer.put(newValue);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			AircraftDisplay display = new AircraftDisplay();
			AircraftFactory factory = new AircraftFactory();
			try {
				display.display(factory.getAircraft(buffer.take()));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		AircraftSentence aircraft = new AircraftSentence("\"40750f\",\"WUK507  \",\"United Kingdom\",1560682369,1560682370,10.3017,48.3471,11582.4,false,230.46,304.54,-0.33,null,11917.68,\"2217\",false,0");
		AircraftFactory fact = new AircraftFactory();
		System.out.println(fact.getAircraft(aircraft));
	}


	
}
