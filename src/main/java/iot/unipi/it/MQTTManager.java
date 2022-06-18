package iot.unipi.it;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MQTTManager implements MqttCallback{
	
	String publisher_topic = "actuator";
	String broker = "tcp://127.0.0.1:1883";
	String clientId = "JavaCollector";
	
	// temperature default values
	double temperatureThreshold = 25; //if > 25 tON
	double lightThreshold = 35; //if < 35 lON
	double currentTemperature = 20;
	double currentLight = 50;
	boolean isTextualLogEnabled = false;
	
	MqttClient mqttClient;
	CoAPManager coapManager;
	
	public MQTTManager(CoAPManager coapManager) throws MqttException{
		
		mqttClient = new MqttClient(broker,clientId);
		mqttClient.setCallback(this);
		mqttClient.connect();
		mqttClient.subscribe("temperature");
		mqttClient.subscribe("light");
		this.coapManager = coapManager;
	}

	public void connectionLost(Throwable cause) {
		System.out.println(cause.getMessage());
		
	}
	
	public double getCurrentTemperature()
	{
		return currentTemperature;
	}
	public double getCurrentLight()
	{
		return currentLight;
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {

		String json_message = new String(message.getPayload());

		//parsing
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		jsonObject = (JSONObject) parser.parse(json_message);
		/*try
		{
			jsonObject = (JSONObject) parser.parse(json_message);
		} catch (ParseException e) {
			e.printStackTrace();
		}*/
		if(jsonObject != null) {
			int value = -1;
			if (topic.equals("temperature")) {
				value = (Integer) jsonObject.get("temperature");
				currentTemperature = value;
			}
			if (topic.equals("light")){
				value = (Integer) jsonObject.get("light");
				currentLight = value;
			}
			
			// get current date
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date d = new Date();
			String[] tokens = dateFormat.format(d).split(" ");
			String date = tokens[0];
			String time = tokens[1];
			
			if(isTextualLogEnabled){
				if(topic.equals("temperature"))
					System.out.println("date:"+date+", time:"+time+", temperature:"+value);

				if(topic.equals("light"))
					System.out.println("date:"+date+", time:"+time+", light:"+value);
				
			}
			valueControl(value, topic);
			storeMqttData(topic, value,date,time);
			
		}
		
	}
	
	public void setTemperatureThreshold(double temperatureThreshold) {
		this.temperatureThreshold = temperatureThreshold;
	}
	
	public double getTemperatureThreshold() {
		return temperatureThreshold;
	}
	
	public void setLightThreshold(double lightThreshold) {
		this.lightThreshold = lightThreshold;
	}
	
	public double getLightThreshold() {
		return lightThreshold;
	}

	public void deliveryComplete(IMqttDeliveryToken token) {}
	
	public void valueControl(int value, String topic) {
		int new_mode;
		String command = "";
		if(topic.equals("temperature")){
			if(value > temperatureThreshold)
				command = "tON"; // decrease the temperature - turn on the temperature actuator
			else 
				command = "tOFF"; // increase the temperature - turn off the temperature actuator
		}
		else if(topic.equals("light")){
			if(value > lightThreshold)
				command = "lOFF"; // turn off the light actuator (bulb)
			else 
				command = "lON"; //turn on the light actuator
		}
		
		//publish on topic 'actuator' for sensor(simulation purpouse)
		try 
		{
			MqttMessage message = new MqttMessage(command.getBytes());
			mqttClient.publish(publisher_topic+"_"+topic, message);
		}catch(MqttException me) 
		{
			me.printStackTrace();
		}

		//MANDARE tramite coapManager POST specificando il topic!!!!! all'attuatore #%
		//Forwarding throught coapManger obj a POST request (by including the topic)
		if(command.equals("tON") || command.equals("lON") )
			coapManager.coAPPOST(topic, 1);
		else if(command.equals("tOFF") || command.equals("lOFF") )
			coapManager.coAPPOST(topic, 0);
		else
			System.out.println("Error in valueControl of MQTTManager");
	}
	
	public static void storeMqttData(String topic, int value,String date,String time) {
		
		  String connectionUrl = "jdbc:mysql://localhost:3306/sensors?serverTimezone=UTC";
		  String query = "INSERT INTO MQTTData (date,time,topic, value) VALUES ('"+date+"','"+time+"',"+topic+","+value+")";
		  
		  try {
			  Connection conn = DriverManager.getConnection(connectionUrl,"root","root");
			  PreparedStatement ps = conn.prepareStatement(query);
			  ps.executeUpdate();
			  conn.close();
		  }
		  catch(SQLException e){
			  e.printStackTrace();
		  }
	
	}
	
}
