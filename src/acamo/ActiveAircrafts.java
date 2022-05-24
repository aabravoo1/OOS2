package acamo;

import java.util.ArrayList;
import java.util.HashMap;

import messer.BasicAircraft;
import observer.Observable;
import observer.Observer;

public class ActiveAircrafts implements ActiveAircraftsInterface, Observer<BasicAircraft>{
	
	private HashMap<String, BasicAircraft> activeAircraft;
	
	public ActiveAircrafts() {
		this.activeAircraft = new HashMap<String, BasicAircraft>();
	}

	@Override
	public void store(String icao, BasicAircraft ac) {
		// TODO Auto-generated method stub
		this.activeAircraft.put(icao, ac);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		this.activeAircraft.clear();
		
	}

	@Override
	public BasicAircraft retrieve(String icao) {
		// TODO Auto-generated method stub
		if(this.activeAircraft.containsKey(icao)) {
			return this.activeAircraft.get(icao);
		}
		return null;
	}
	
	public String toString() {
		return this.activeAircraft.toString();
	}
	
	@Override
	public ArrayList<BasicAircraft> values() {
		// TODO Auto-generated method stub
		ArrayList<BasicAircraft> array = new ArrayList<BasicAircraft>(this.activeAircraft.values());
		
		return array;
	}

	@Override
	public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
		// TODO Auto-generated method stub
		this.activeAircraft.put(newValue.getIcao(), newValue);
	}

}
