package acamo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.saring.leafletmap.*;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Acamo extends Application implements  Observer<BasicAircraft> {
	private  double latitude = 48.7433425;
    private  double longitude = 9.3201122;
    private static boolean haveConnection = true;
    
    private ObservableList<BasicAircraft> aircraftList = FXCollections.observableArrayList(); 
    private ArrayList<String> fields;
    private TableView<BasicAircraft> table = new TableView<BasicAircraft>();
    //private HashMap<String, Label> aircraftLabelMap;
    private HashMap<String, Marker> aircraftMarkerMap = new HashMap<String,Marker>();
    private ActiveAircrafts activeAircrafts;
    //private ArrayList<Label> aircraftLabelList;
    private BasicAircraft selectedAircraft;
    private int selectedIndex = 0;
    private LeafletMapView mapView;
    
    private CompletableFuture<Worker.State> loadState;
    private Marker homeMarker;
    private int markerNumber;
    
    final VBox aircraftBox = new VBox();

	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		String urlString = "https://opensky-network.org/api/states/all";
		PlaneDataServer server;
		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 70);
		else
			server = new PlaneDataServer(latitude, longitude, 100);

		Senser senser = new Senser(server);
		new Thread(server).start();
		new Thread(senser).start();
		
		Messer messer = new Messer();
		senser.addObserver(messer);
		new Thread(messer).start();
		
		activeAircrafts = new ActiveAircrafts();
		messer.addObserver(activeAircrafts);
		messer.addObserver(this);
		
		//for map View
		
		mapView = new LeafletMapView();
		mapView.setLayoutX(0);
		mapView.setLayoutY(0);
		mapView.setMaxWidth(640);
		List<MapLayer> config = new LinkedList<>();
		config.add(MapLayer.OPENSTREETMAP);
		
		// Record the load state
		loadState = mapView.displayMap(
			new MapConfig(config,
			new ZoomControlConfig(),
			new ScaleControlConfig(),
			new LatLong(latitude, longitude)));
		
		mapView.setPrefSize(500, 400);
		
		//For the MapView config
	    loadState.whenComplete((state, throwable) -> {
	    	mapView.addCustomMarker("HOME", "icons/basestation.png");
            homeMarker = new Marker(new LatLong(latitude, longitude), "HOME","HOME", 0);
            mapView.addMarker(homeMarker);
            
            mapView.onMapClick((LatLong latlong) -> {
    	    	// use the new coordinates to reset the map
            	restartServer(latlong,server);
    	    	});
            
	    	});
	    
		final VBox mapBox = new VBox();
		mapBox.setSpacing(5);
		mapBox.setPadding(new Insets(10,0,0,10));
		
		// Button 1
		Button button1 = new Button("Send");
		// TextField
		TextField textField1 = new TextField("Latitude");
		textField1.setPrefWidth(110);
		// TextField
		TextField textField2 = new TextField("Longitude");
		textField2.setPrefWidth(110);
		//textField2.setText("");
		
		Label latitudeLabel = new Label("Latitude");
		latitudeLabel.setFont(new Font("Arial", 12));
		Label longitudeLabel = new Label("Longitude");
		longitudeLabel.setFont(new Font("Arial", 12));
		
		button1.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        //label.setText("Accepted");
		    	LatLong latlong = new LatLong(Double.parseDouble(textField1.getText()),Double.parseDouble(textField2.getText()));
		    	restartServer(latlong,server);
		    }
		});
		
		
		
		mapBox.getChildren().addAll(mapView,latitudeLabel,textField1,longitudeLabel, textField2, button1, table);
		
		//For table
		fields = BasicAircraft.getAttributesNames();
		
		for(int i = 0; i<fields.size(); i++) {//set columns
			TableColumn<BasicAircraft, String> newColumn = new TableColumn<BasicAircraft, String>(fields.get(i));
			newColumn.setCellValueFactory(new PropertyValueFactory<BasicAircraft,String>(fields.get(i)));
			table.getColumns().add(newColumn);
		}
		
		table.setItems(aircraftList);	
		table.setEditable(false);
		table.autosize();
		table.setPlaceholder(new Label("Waiting for Planes"));
		
		table.setOnMousePressed(new EventHandler<MouseEvent>() {//Add event handler for selected aircraft
			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				if(event.isPrimaryButtonDown()) {
					selectedIndex = table.getSelectionModel().getSelectedIndex();
					
					selectedAircraft = table.getSelectionModel().getSelectedItem();
					//aircraftMarkerMap.clear();
					refresh(selectedAircraft);		
				}
			}
		});
		
		final Label activeLabel = new Label("Active Aircrafts");
		activeLabel.setFont(new Font("Arial", 20));
		
		final VBox tableBox = new VBox();
		tableBox.setSpacing(5);
		tableBox.setPadding(new Insets(10,0,0,10));
		tableBox.getChildren().addAll(activeLabel,table);
		
		final Label selectedLabel = new Label("Selected");
		selectedLabel.setFont(new Font("Arial", 20));
		
		final VBox selectedBox = new VBox();
		selectedBox.setSpacing(5);
		selectedBox.setPadding(new Insets(10,0,0,10));
		selectedBox.getChildren().addAll(selectedLabel);
		
		for(int i = 0; i<fields.size(); i++) {//set ids for selected aircraft
			final Label fieldLabel = new Label(fields.get(i));
			fieldLabel.setFont(new Font("Arial", 15));
			selectedBox.getChildren().add(fieldLabel);
		}
		
		final Label aircraftLabel = new Label("Aircraft");
		aircraftLabel.setFont(new Font("Arial", 20));
		
		aircraftBox.setSpacing(5);
		aircraftBox.setPadding(new Insets(10,0,0,10));
		aircraftBox.getChildren().addAll(aircraftLabel);
		
		HBox root = new HBox();
		root.setSpacing(10);
		root.setPadding(new Insets(15,20,10,10));
		
		root.getChildren().addAll(mapBox,tableBox,selectedBox,aircraftBox);
		
		//create layout of the table and pane for selected aircraft
		
		Scene scene = new Scene(root);
	    //root.setPadding(new Insets(5));
	    //root.getChildren().add(table);		    	 
	    stage.setTitle("TableView ACAMO");
		//Scene scene = new Scene(root, 485, 300);
	    stage.sizeToScene();
	    stage.setOnCloseRequest(e -> System.exit(0));
	    stage.setScene(scene);
	    stage.show();
	}
	
	public void reloadMap() {
		loadState.whenComplete((state, throwable) -> {
	    	
            for(int i = 0; i < this.aircraftList.size(); i++) {
            	if(!this.aircraftMarkerMap.containsKey(this.aircraftList.get(i).getIcao())) {
            		createPlaneMarker(this.aircraftList.get(i));
    				this.markerNumber++;
    				System.out.println(this.markerNumber + "," + this.aircraftList.size()+ "," +i);
    			}else {
    				//mapView.removeMarker(this.aircraftMarkerMap.get(this.aircraftList.get(i).getIcao()));
    				//createPlaneMarker(this.aircraftList.get(i));
    				Marker move = this.aircraftMarkerMap.get(this.aircraftList.get(i).getIcao());
    				move.move(new LatLong(this.aircraftList.get(i).getCoordinate().getLatitude(),this.aircraftList.get(i).getCoordinate().getLongitude()));
    				System.out.println("moved"+this.aircraftList.get(i).getIcao());
    				//System.out.println(this.markerNumber+ "," + this.aircraftList.size()+ "," +i);
    			}
            }
    	});
	}
	
	public void restartServer(LatLong latlong, PlaneDataServer server) {
		for(int i=0; i<this.aircraftList.size();i++) {
    		mapView.removeMarker(this.aircraftMarkerMap.get(this.aircraftList.get(i).getIcao()));
    	}
    	this.aircraftList.clear();
    	this.activeAircrafts.clear();
    	this.table.refresh();
    	server.resetLocation(latlong.getLatitude(), latlong.getLongitude(), 70);
    	
    	mapView.panTo(latlong);
    	System.out.println(latlong.getLatitude()+" "+ latlong.getLongitude());
    	this.homeMarker.move(latlong);
    	this.latitude = latlong.getLatitude();
    	this.longitude = latlong.getLongitude();
	}

	public void createPlaneMarker(BasicAircraft aircraft) {
		String icon = "icons/plane06.png";
		double trak = aircraft.getTrak();
		if(trak>=352 && trak<360 || trak>=0 && trak<7) {
			icon = "icons/plane06.png";
		}else if(trak>=7 && trak<22) {
			icon = "icons/plane05.png";
		}else if(trak>=22 && trak<37) {
			icon = "icons/plane04.png";
		}else if(trak>=37 && trak<52) {
			icon = "icons/plane03.png";
		}else if(trak>=52 && trak<67) {
			icon = "icons/plane02.png";
		}else if(trak>=67 && trak<82) {
			icon = "icons/plane01.png";
		}else if(trak>=82 && trak<97) {
			icon = "icons/plane00.png";
		}else if(trak>=97 && trak<112) {
			icon = "icons/plane23.png";
		}else if(trak>=112 && trak<127) {
			icon = "icons/plane22.png";
		}else if(trak>=127 && trak<142) {
			icon = "icons/plane21.png";
		}else if(trak>=142 && trak<157) {
			icon = "icons/plane20.png";
		}else if(trak>=157 && trak<172) {
			icon = "icons/plane19.png";
		}else if(trak>=172 && trak<187) {
			icon = "icons/plane18.png";
		}else if(trak>=187 && trak<202) {
			icon = "icons/plane17.png";
		}else if(trak>=202 && trak<217) {
			icon = "icons/plane16.png";
		}else if(trak>=217 && trak<232) {
			icon = "icons/plane15.png";
		}else if(trak>=232 && trak<247) {
			icon = "icons/plane14.png";
		}else if(trak>=247 && trak<262) {
			icon = "icons/plane13.png";
		}else if(trak>=262 && trak<277) {
			icon = "icons/plane12.png";
		}else if(trak>=277 && trak<292) {
			icon = "icons/plane11.png";
		}else if(trak>=292 && trak<307) {
			icon = "icons/plane10.png";
		}else if(trak>=307 && trak<322) {
			icon = "icons/plane09.png";
		}else if(trak>=322 && trak<337) {
			icon = "icons/plane08.png";
		}else if(trak>=337 && trak<352) {
			icon = "icons/plane07.png";
		}else {
			System.out.println("ERROR: "+trak);
		}
		
		mapView.addCustomMarker("PLANE", icon);
		Marker plane = new Marker(new LatLong(aircraft.getCoordinate().getLatitude(), aircraft.getCoordinate().getLongitude()), aircraft.getIcao(),"PLANE", 0);
		mapView.addMarker(plane);
        this.aircraftMarkerMap.put(aircraft.getIcao(), plane);
	}

	@Override
	public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
		// TODO Auto-generated method stub
		//this.aircraftList.clear();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				aircraftList.clear();
				aircraftList.addAll(activeAircrafts.values());
				table.getSelectionModel().select(selectedIndex);
				//selectedIndex = table.getSelectionModel().getSelectedIndex();
				//selectedAircraft = aircraftList.get(0);
				BasicAircraft selectedAircraft = table.getSelectionModel().getSelectedItem();
				//System.out.println("selected aircraft: " + selectedAircraft);
				//System.out.println("selected index: " + selectedIndex);
				refresh(selectedAircraft);
				reloadMap();
			}
		});		
	}
	
	public void refresh(BasicAircraft selectedAircraft) {
		aircraftBox.getChildren().clear();
		final Label aircraftLabel = new Label("Aircraft");
		aircraftLabel.setFont(new Font("Arial", 20));
		aircraftBox.getChildren().addAll(aircraftLabel);
		
		ArrayList<Object> attributesValues = BasicAircraft.getAttributesValues(selectedAircraft);
		
		for(int i = 0; i<6; i++) {//set values for selected aircraft
			final Label fieldLabel = new Label(attributesValues.get(i).toString());
			fieldLabel.setFont(new Font("Arial", 15));
			aircraftBox.getChildren().add(fieldLabel);
		}
	}
	
	public static void main(String[] args) {
	      launch(args); 
	  }
	
	ChoiceBox bikeChoiceBox = new ChoiceBox();
	final TextField messageField = new TextField(); 
	final TextField destField = new TextField();
	
	class HandlerClass implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println(bikeChoiceBox.getValue()+", "+messageField.getText()+", "+destField.getText());
		}
		
	}
	
}



/*51.33061163769853 6.8115234375
 * /*
            	for(int i=0; i<this.aircraftList.size();i++) {
            		mapView.removeMarker(this.aircraftMarkerMap.get(this.aircraftList.get(i).getIcao()));
            	}
            	this.aircraftList.clear();
            	server.resetLocation(latlong.getLatitude(), latlong.getLongitude(), 70);
            	
            	mapView.mapMove(latlong.getLatitude(), latlong.getLongitude());
            	System.out.println(latlong.getLatitude()+" "+ latlong.getLongitude());
    	    	this.homeMarker.move(latlong);
    	    	this.latitude = latlong.getLatitude();
    	    	this.longitude = latlong.getLongitude();
    	    	*/

