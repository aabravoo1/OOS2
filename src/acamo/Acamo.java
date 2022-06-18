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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    private  double currentLat = 48.7433425;
    private  double currentLong = 9.3201122;
    private static boolean haveConnection = true;
    
    private ObservableList<BasicAircraft> aircraftList = FXCollections.observableArrayList(); 
    private ArrayList<String> fields;
    private TableView<BasicAircraft> table = new TableView<BasicAircraft>();
    private HashMap<String, Label> aircraftLabelMap;
    private HashMap<String, Marker> aircraftMarkerMap;
    private ActiveAircrafts activeAircrafts;
    private ArrayList<Label> aircraftLabelList;
    private BasicAircraft selectedAircraft;
    private int selectedIndex = 0;
    private LeafletMapView mapView;
    
    private CompletableFuture<Worker.State> loadState;
    private ArrayList<Marker> markerList;
    //private Marker plane;
    private Marker homeMarker;
    
    final VBox aircraftBox = new VBox();

	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		String urlString = "https://opensky-network.org/api/states/all";
		PlaneDataServer server;
		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 50);
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
		
		/*LeafletMapView map = new LeafletMapView();
		
		LatLong position = new LatLong(this.latitude, this.longitude);
		MapConfig config = new MapConfig();
		//map.setView(position, 10);
		map.setPrefSize(500, 500);
		
		map.displayMap(config);
		
		map.setView(position, 10);
		//map.setView(position, 18);*/
		
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
		/*Marker position = new Marker(
    			new LatLong(this.currentLat,this.currentLong),
    			"","",0);
		
		*///For the MapView config
	    loadState.whenComplete((state, throwable) -> {
	    	// do all map building here
	    	
	    	mapView.addCustomMarker("HOME", "icons/basestation.png");
            homeMarker = new Marker(new LatLong(latitude, longitude), "HOME","HOME", 0);
            mapView.addMarker(homeMarker);
            
            mapView.onMapClick((LatLong latlong) -> {
    	    	// use the new coordinates to reset the map
            	mapView.mapMove(latlong.getLatitude(), latlong.getLongitude());
            	System.out.println(latlong.getLatitude()+" "+ latlong.getLongitude());
    	    	this.homeMarker.move(latlong);
    	    	this.latitude = latlong.getLatitude();
    	    	this.longitude = latlong.getLongitude();
    	    	server.resetLocation(this.latitude, this.longitude, 0);
    	    });
            
	    	});
	    this.aircraftMarkerMap = new HashMap<String,Marker>();
	    //reloadMap();
	    
		
		final VBox mapBox = new VBox();
		mapBox.setSpacing(5);
		mapBox.setPadding(new Insets(10,0,0,10));
		mapBox.getChildren().addAll(mapView,table);
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
	    	// do all map building here
			/*if(!this.aircraftMarkerMap.containsKey(this.aircraftList.get(0).getIcao())) {
				//System.out.println(this.aircraftMarkerMap.containsKey(this.aircraftList.get(0).getIcao()));
				createPlaneMarker(this.aircraftList.get(0));
				
				
			}else {
				//System.out.println(this.aircraftMarkerMap.containsKey(this.aircraftList.get(0).getIcao()));
				mapView.removeMarker(this.aircraftMarkerMap.get(this.aircraftList.get(0).getIcao()));
				createPlaneMarker(this.aircraftList.get(0));
				//this.plane.move(new LatLong(this.selectedAircraft.getCoordinate().getLatitude(), this.selectedAircraft.getCoordinate().getLongitude()));
				
			}*/
			//this.markerList.add(plane);
            
            for(int i = 0; i < this.aircraftList.size()-1; i++) {
            	if(!this.aircraftMarkerMap.containsKey(this.aircraftList.get(i).getIcao())) {
    				createPlaneMarker(this.aircraftList.get(i));
    			}else {
    				//System.out.println(this.aircraftMarkerMap.containsKey(this.aircraftList.get(0).getIcao()));
    				mapView.removeMarker(this.aircraftMarkerMap.get(this.aircraftList.get(i).getIcao()));
    				createPlaneMarker(this.aircraftList.get(i));
    				//this.plane.move(new LatLong(this.selectedAircraft.getCoordinate().getLatitude(), this.selectedAircraft.getCoordinate().getLongitude()));
    				System.out.println();
    			}
            }
	    	});
	}

	public void createPlaneMarker(BasicAircraft aircraft) {
		String icon = null;
		if(aircraft.getTrak()>=352 && aircraft.getTrak()<7) {
			icon = "icons/plane06.png";
		}else if(aircraft.getTrak()>=7 && aircraft.getTrak()<22) {
			icon = "icons/plane05.png";
		}else if(aircraft.getTrak()>=22 && aircraft.getTrak()<37) {
			icon = "icons/plane04.png";
		}else if(aircraft.getTrak()>=37 && aircraft.getTrak()<52) {
			icon = "icons/plane03.png";
		}else if(aircraft.getTrak()>=52 && aircraft.getTrak()<67) {
			icon = "icons/plane02.png";
		}else if(aircraft.getTrak()>=67 && aircraft.getTrak()<82) {
			icon = "icons/plane01.png";
		}else if(aircraft.getTrak()>=82 && aircraft.getTrak()<97) {
			icon = "icons/plane00.png";
		}else if(aircraft.getTrak()>=97 && aircraft.getTrak()<112) {
			icon = "icons/plane23.png";
		}else if(aircraft.getTrak()>=112 && aircraft.getTrak()<127) {
			icon = "icons/plane22.png";
		}else if(aircraft.getTrak()>=127 && aircraft.getTrak()<142) {
			icon = "icons/plane21.png";
		}else if(aircraft.getTrak()>=142 && aircraft.getTrak()<157) {
			icon = "icons/plane20.png";
		}else if(aircraft.getTrak()>=157 && aircraft.getTrak()<172) {
			icon = "icons/plane19.png";
		}else if(aircraft.getTrak()>=172 && aircraft.getTrak()<187) {
			icon = "icons/plane18.png";
		}else if(aircraft.getTrak()>=187 && aircraft.getTrak()<202) {
			icon = "icons/plane17.png";
		}else if(aircraft.getTrak()>=202 && aircraft.getTrak()<217) {
			icon = "icons/plane16.png";
		}else if(aircraft.getTrak()>=217 && aircraft.getTrak()<232) {
			icon = "icons/plane15.png";
		}else if(aircraft.getTrak()>=232 && aircraft.getTrak()<247) {
			icon = "icons/plane14.png";
		}else if(aircraft.getTrak()>=247 && aircraft.getTrak()<262) {
			icon = "icons/plane13.png";
		}else if(aircraft.getTrak()>=262 && aircraft.getTrak()<277) {
			icon = "icons/plane12.png";
		}else if(aircraft.getTrak()>=277 && aircraft.getTrak()<292) {
			icon = "icons/plane11.png";
		}else if(aircraft.getTrak()>=292 && aircraft.getTrak()<307) {
			icon = "icons/plane10.png";
		}else if(aircraft.getTrak()>=307 && aircraft.getTrak()<322) {
			icon = "icons/plane09.png";
		}else if(aircraft.getTrak()>=322 && aircraft.getTrak()<337) {
			icon = "icons/plane08.png";
		}else if(aircraft.getTrak()>=337 && aircraft.getTrak()<352) {
			icon = "icons/plane07.png";
		}
		
		mapView.addCustomMarker("PLANE", icon);
		Marker plane = new Marker(new LatLong(aircraft.getCoordinate().getLatitude(), aircraft.getCoordinate().getLongitude()), aircraft.getIcao(),"PLANE", 0);
        mapView.addMarker(plane);
        //aircraft.setMarker(true);
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
		
		//fields = BasicAircraft.getAttributesNames();
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
}

