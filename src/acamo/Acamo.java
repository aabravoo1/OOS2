package acamo;

import java.util.ArrayList;

import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Acamo extends Application implements  Observer<BasicAircraft> {
	private static double latitude = 48.7433425;
    private static double longitude = 9.3201122;
    private static boolean haveConnection = true;
    private ObservableList<BasicAircraft> aircraftList; 

	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		String urlString = "https://opensky-network.org/api/states/all";
		PlaneDataServer server;
		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 150);
		else
			server = new PlaneDataServer(latitude, longitude, 100);

		Senser senser = new Senser(server);
		new Thread(server).start();
		new Thread(senser).start();
		
		Messer messer = new Messer();
		senser.addObserver(messer);
		new Thread(messer).start();
		
		ActiveAircrafts activeAircrafts = new ActiveAircrafts();
		messer.addObserver(activeAircrafts);
		messer.addObserver(this);
		
		ArrayList<String> fields = BasicAircraft.getAttributesNames();
		TableView<BasicAircraft> table = new TableView<BasicAircraft>();
		
		
		for(int i = 0; i<fields.size(); i++) {
			table.setItems(aircraftList);
			table.setEditable(false);
			table.autosize();
			
			//create layout of the table and pane for selected aircraft
			//Add event handler for selected aircraft
			
		}
	}

	@Override
	public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
		// TODO Auto-generated method stub
		this.aircraftList.add(newValue);
	}
}

