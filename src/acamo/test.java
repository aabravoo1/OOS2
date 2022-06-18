package acamo;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import javafx.event.EventHandler;

import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

public class test extends Application implements Observer<BasicAircraft> 
{
	
	final Label tdatalabel = new Label("Init");
	final Label sdatalabel = new Label("Init");
	
	private static double latitude = 48.7433425;
    private static double longitude = 9.3201122;
    private static boolean haveConnection = true;
 
    public static void main(String[] args) {
		launch(args);
    }
 
    @Override
    public void start(Stage stage) {
    	String urlString = "https://opensky-network.org/api/states/all";
		PlaneDataServer server;
		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 150);
		else
			server = new PlaneDataServer(latitude, longitude, 100);

		Senser senser = new Senser(server);

		new Thread(senser).start();
		
		Messer messer = new Messer();
		senser.addObserver(messer);
		new Thread(messer).start();
		
		// activeAircrafts = new ActiveAircrafts();
		// messer.addObserver(activeAircrafts);
		messer.addObserver(this);
		
        final Label tlabel = new Label("Active Aircrafts");
        tlabel.setFont(new Font("Arial", 20));
        tdatalabel.setFont(new Font("Arial", 20));
 
        final VBox tbox = new VBox(); // for table
        tbox.setSpacing(5);
        tbox.setPadding(new Insets(10, 0, 0, 10));
        tbox.getChildren().addAll(tlabel, tdatalabel);
 
        final Label slabel = new Label("Operator                 ");
        slabel.setFont(new Font("Arial", 20));
        sdatalabel.setFont(new Font("Arial", 20));

        final VBox slbox = new VBox(); // for selected labels
        slbox.setSpacing(5);
        slbox.setPadding(new Insets(10, 0, 0, 10));
        slbox.getChildren().addAll(slabel, sdatalabel);

        
 
        HBox root = new HBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15,20, 10,10));
        
		root.getChildren().addAll(tbox, slbox);
 
		Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Acamo");
        stage.sizeToScene();
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft aircraft) {
    	//updateWrong(observable, aircraft);
    	updateCorrect(observable, aircraft);
    }

    public void updateWrong(Observable<BasicAircraft> observable, BasicAircraft aircraft) {
    	
		tdatalabel.setText(aircraft.getIcao());
		sdatalabel.setText(aircraft.getOperator().toString());
    }
    
    public void updateCorrect(Observable<BasicAircraft> observable, BasicAircraft aircraft) {
    	
    	Platform.runLater(new Runnable() {
    		@Override 
    		public void run() {
    			tdatalabel.setText(aircraft.getIcao());
    			sdatalabel.setText(aircraft.getOperator().toString());
    		}
    	});
    	
    	// Platform.runLater(()-> {
    }
}
