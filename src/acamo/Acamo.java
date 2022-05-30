package acamo;

import java.util.ArrayList;
import java.util.HashMap;

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
	private static double latitude = 48.7433425;
    private static double longitude = 9.3201122;
    private static boolean haveConnection = true;
    
    private ObservableList<BasicAircraft> aircraftList = FXCollections.observableArrayList(); 
    private ArrayList<String> fields;
    private TableView<BasicAircraft> table = new TableView<BasicAircraft>();
    private HashMap<String, Label> aircraftLabelMap;
    private ActiveAircrafts activeAircrafts;
    private ArrayList<Label> aircraftLabelList;
    private BasicAircraft selectedAircraft;
    private int selectedIndex = 0;
    
    final VBox aircraftBox = new VBox();

	public void start(Stage stage) throws Exception {
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
		
		activeAircrafts = new ActiveAircrafts();
		messer.addObserver(activeAircrafts);
		messer.addObserver(this);
		
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
		
		table.setOnMousePressed(new EventHandler<MouseEvent>() {

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
		
		/*//final LabelList aircraft
		for(int i = 0; i<fields.size(); i++) {
			aircraftLabelMap.put(fields.get(i), aircraftLabel);
			aircraftLabelList.add(aircraftLabel);
		}*/
		//aircraftBox.getChildren().addAll(aircraftLabelList);
		aircraftBox.getChildren().addAll(aircraftLabel);
		
		HBox root = new HBox();
		root.setSpacing(10);
		root.setPadding(new Insets(15,20,10,10));
		
		root.getChildren().addAll(tableBox,selectedBox,aircraftBox);
		
		
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
		//Add event handler for selected aircraft
	    
	    
	    /*
	    ObservableList<BasicAircraft> selectedItems = table.getSelectionModel().getSelectedItems();

    	selectedItems.addListener(
    	  new ListChangeListener<BasicAircraft>() {
    	    @Override
    	    public void onChanged(
    	      Change<? extends BasicAircraft> change) {
    	        //System.out.println(
    	          //"Selection changed: " + change.getList());
    	        
    	        //selectedIndex = table.getSelectionModel().getSelectedIndex();
    	        selectedAircraft = table.getSelectionModel().getSelectedItem();
    	        System.out.println("Selection index: " + selectedIndex);
    	        System.out.println("selected aircraft: " + selectedAircraft);
    	        refresh(selectedAircraft);
    	      }
    	});*/
		
	}

	@Override
	public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
		// TODO Auto-generated method stub
		//this.aircraftList.clear();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				aircraftList.clear();
				aircraftList.addAll(activeAircrafts.values());
				
				
				table.getSelectionModel().select(selectedIndex);
				//selectedIndex = table.getSelectionModel().getSelectedIndex();
				//selectedAircraft = aircraftList.get(0);
				BasicAircraft selectedAircraft = table.getSelectionModel().getSelectedItem();
				//System.out.println("selected aircraft: " + selectedAircraft);
				//System.out.println("selected index: " + selectedIndex);
				refresh(selectedAircraft);
			}
			
		});		
	}
	
	public void refresh(BasicAircraft selectedAircraft) {
		//table.getSelectionModel().select(selectedIndex);
		
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

