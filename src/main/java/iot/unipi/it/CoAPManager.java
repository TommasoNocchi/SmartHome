package iot.unipi.it;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CoAPManager extends CoapServer {
	private static boolean isTextualLogEnabled = false;
	private static boolean isAutomationModeEnabled = true;
	private static ArrayList<String> temperatureActuatorsAddress;
	private static ArrayList<String> lightActuatorsAddress;
	private static CoAPResourceExample registration; 
	
	public CoAPManager() {
		registration = new CoAPResourceExample("hello");
		this.add(registration);
		this.start();
		this.temperatureActuatorsAddress = new ArrayList<String>();
		this.lightActuatorsAddress = new ArrayList<String>();
	}

	public static void addNewTemperatureActuator(InetAddress newInetAddr, int newPort){
		temperatureActuatorsAddress.add(newInetAddr.toString().replace("/", ""));
	}

	public static void addNewLightActuator(InetAddress newInetAddr, int newPort){
		lightActuatorsAddress.add(newInetAddr.toString().replace("/", ""));
	}
	
	public void enableTextualLog()
	{
		isTextualLogEnabled = true;
	}
	
	public void disableTextualLog()
	{
		isTextualLogEnabled = false;
	}

	public void enableAutomationMode()
	{
		isAutomationModeEnabled = true;
	}

	public void disableAutomationMode()
	{
		isAutomationModeEnabled = false;
	}
	
	public boolean getIsTextualLogEnabled()
	{
		return isTextualLogEnabled;
	}

	public boolean getAutomationMode(){ return isAutomationModeEnabled; }


	public void coAPPOST(String topic, int value){

		if(!isAutomationModeEnabled)
			return;

		// get current date for storing in DB
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date d = new Date();
		String[] tokens = dateFormat.format(d).split(" ");
		String date = tokens[0];
		String time = tokens[1];
		
		if(topic.equals("temperature")){
			for(String temperatureActuatorsAddres: temperatureActuatorsAddress)
				actuatorPOST(temperatureActuatorsAddres, topic, value);
		}
		else if(topic.equals("light")){
			for(String lightActuatorsAddres: lightActuatorsAddress)
				actuatorPOST(lightActuatorsAddres, topic, value);
		}
		
		CoAPManager.storeData(topic, value, date, time);
	}

	public void actuatorPOST(final String addr, final String topic, final int value){
		Thread t = new Thread() {
		    public void run() {
		    	CoapClient client;
		    	CoapResponse response;
		    	String code;
		    	//String actuatorURI ="coap://[fd00::20"+node_id+":"+node_id+":"+node_id+":"+node_id+"]/"+endpointBulb;
				String actuatorURI ="coap://["+addr+"]/casa -e name="+topic+"'\'&value="+value;

		    	/*
		    	String postPayload="";
				if(light>rooms[(int)(node_id-2)].getHighLuminosityThreshold()) 
					postPayload="off";
				
				else if(light>rooms[(int)(node_id-2)].getMediumLuminosityThreshold())
					postPayload="low";
				
				else if(light>rooms[(int)(node_id-2)].getLowLuminosityThreshold())
					postPayload="medium";

				else
					postPayload="high";*/
				
				client = new CoapClient(actuatorURI);
				response = client.post("topic="+topic,MediaTypeRegistry.TEXT_PLAIN); //NON PENSO FUNZIONI (#%)

				code = response.getCode().toString();
				if(isTextualLogEnabled){
					System.out.println("Response from "+addr+": "+ code);
				}
				if(code == null)
					System.out.println("---Error actuatorPOST CoAPManager: "+code);
		    }  
		};
		t.start();
	}
	
	public static void storeData(String topic,int value, String date,String time) 
	{
		  String connectionUrl = "jdbc:mysql://localhost:3306/sensors?serverTimezone=UTC";
		  String query = "INSERT INTO CoAPData (date,time,topic, value) VALUES ('"+date+"','"+time+"',"+topic+","+value+")";
		  
		  try 
		  {
			  Connection conn = DriverManager.getConnection(connectionUrl,"root","root");
			  PreparedStatement ps = conn.prepareStatement(query);
			  ps.executeUpdate();
			  conn.close();
		  }
		  catch(SQLException e)
		  {
			  e.printStackTrace();
		  }
	}
}
