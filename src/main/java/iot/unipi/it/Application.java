package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Application 
{
	private static MQTTManager mqttManager;
	private static CoAPManager coapManager;
	
	public static void main(String[] args) 
	{	
		CoAPInitialization();
		
		MqttInitialization();

		welcomePrint();
		
		printCommands();
		
		readInput();	
	}
	
	public static void readInput()
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String command = "";
		String[] chuncks;
		double temperatureThreshold;
		int lightThreshold;
		
		while(true) {
			System.out.print("Insert a command or type 'help'\n>");
			try {
				command = bufferedReader.readLine();
				chuncks = command.split(" ");	

				if(chuncks[0].equals("quit") && chuncks.length == 1)
					System.exit(0);
				
				else if(chuncks[0].equals("help") && chuncks.length == 1)
					printCommands();
				
				
				else if(chuncks[0].equals("setTemp") && chuncks.length == 2)
				{
					temperatureThreshold = Double.parseDouble(chuncks[1]);
					mqttManager.setTemperatureThreshold(temperatureThreshold);
					System.out.println("Command rightly executed");
				}

				else if(chuncks[0].equals("setLum") && chuncks.length == 2)
				{
					lightThreshold = Integer.parseInt(chuncks[1]);
					mqttManager.setLightThreshold(lightThreshold);
					System.out.println("Command rightly executed");
				}
				
				/*
				else if(chuncks[0].equals("setLumThresholds") && chuncks.length == 4)
				{
					if(Long.parseLong(chuncks[1]) >= 0 && 
						Long.parseLong(chuncks[1]) < Long.parseLong(chuncks[2]) &&
						Long.parseLong(chuncks[2]) < Long.parseLong(chuncks[3]) &&
						Long.parseLong(chuncks[3]) <= 100)
					{
						coapManager.setAllRoomsLowLuminosityThresholds(Long.parseLong(chuncks[1]));
						coapManager.setAllRoomsMediumLuminosityThresholds(Long.parseLong(chuncks[2]));
						coapManager.setAllRoomsHighLuminosityThresholds(Long.parseLong(chuncks[3]));
						System.out.println("Command rightly executed");
					}
					else
						System.out.println("Error: thresholds are not properly specified");
				}
				
				else if(chuncks[0].equals("setLumThresholds") && chuncks.length == 5)
				{
					if(Long.parseLong(chuncks[1]) >= 0 && 
						Long.parseLong(chuncks[1]) < Long.parseLong(chuncks[2]) &&
						Long.parseLong(chuncks[2]) < Long.parseLong(chuncks[3]) &&
						Long.parseLong(chuncks[3]) <= 100 &&
						coapManager.isRoomValid(chuncks[4]))
					{
						coapManager.setRoomLowLuminosityThreshold(Long.parseLong(chuncks[1]), chuncks[4]);
						coapManager.setRoomMediumLuminosityThreshold(Long.parseLong(chuncks[2]), chuncks[4]);
						coapManager.setRoomHighLuminosityThreshold(Long.parseLong(chuncks[3]), chuncks[4]);
						System.out.println("Command rightly executed");
					}
					else
						System.out.println("Error: parameters are not properly specified");
				}

				else if(chuncks[0].equals("setLowLumThreshold") && chuncks.length == 3)
				{
					if(coapManager.isRoomValid(chuncks[2]))
					{
						if(Long.parseLong(chuncks[1]) >= 0 && Long.parseLong(chuncks[1]) < coapManager.getRoomMediumLuminosityThreshold(chuncks[2]))
						{
							coapManager.setRoomLowLuminosityThreshold(Long.parseLong(chuncks[1]), chuncks[2]);
							System.out.println("Command rightly executed");
						}
						else
							System.out.println("Error: the low luminosity threshold must be lower than the medium luminosity threshold");					
						
					}
					else
						System.out.println("The room you specified does not exist");
				}

				else if(chuncks[0].equals("setMediumLumThreshold") && chuncks.length == 3)
				{
					if(coapManager.isRoomValid(chuncks[2]))
					{
						if(Long.parseLong(chuncks[1]) > coapManager.getRoomLowLuminosityThreshold(chuncks[2]) &&
								Long.parseLong(chuncks[1]) < coapManager.getRoomHighLuminosityThreshold(chuncks[2]))
						{
							coapManager.setAllRoomsMediumLuminosityThresholds(Long.parseLong(chuncks[1]));
							System.out.println("Command rightly executed");
						}
						
						else
							System.out.println("Error: the medium luminosity threshold must be higher than the low luminosity threshold and lower than the high luminosity threshold");					
						
					}
					else
						System.out.println("The room you specified does not exist");							

				}

				
				else if(chuncks[0].equals("setHighLumThreshold") && chuncks.length == 3)
				{
					if(coapManager.isRoomValid(chuncks[2]))
					{
						if(Long.parseLong(chuncks[1]) > coapManager.getRoomMediumLuminosityThreshold(chuncks[2]) && 
								Long.parseLong(chuncks[1]) <= 100)
						{
							coapManager.setAllRoomsHighLuminosityThresholds(Long.parseLong(chuncks[1]));
							System.out.println("Command rightly executed");
						}
						
						else
							System.out.println("Error: the high luminosity threshold must be higher than the medium luminosity threshold");					
						
					}
					else
						System.out.println("The room you specified does not exist");														
				}

				else if(chuncks[0].equals("getCurrLum") && chuncks.length == 2)
				{
					if(coapManager.isRoomValid(chuncks[1]))
						System.out.println("The current luminosity is " + coapManager.getRoomCurrentLuminosity(chuncks[1]) + " lux");										
					else
						System.out.println("The room you specified does not exist");														
					
				}
				*/
				else if(chuncks[0].equals("getCurrTemp") && chuncks.length == 1)
					System.out.println("The current temperature is " + mqttManager.getCurrentTemperature() + "°C");

				else if(chuncks[0].equals("getCurrLum") && chuncks.length == 1)
					System.out.println("The current temperature is " + mqttManager.getCurrentLight() + " lux");

				else if(chuncks[0].equals("getLumThreshold") && chuncks.length == 1) {
						System.out.println("Luminosity thresholds is: "
								+ "\n\tlow luminosity: " + mqttManager.getLightThreshold());
				}

				else if(chuncks[0].equals("getTemperatureThreshold") && chuncks.length == 1) {
					System.out.println("Luminosity thresholds is: "
							+ "\n\tlow luminosity: " + mqttManager.getTemperatureThreshold());
				}
				
				else if(chuncks[0].equals(" ") && chuncks.length == 1)
					continue;
				
				else if(chuncks[0].equals("showTextLog") && chuncks.length == 1)
				{
					if(!coapManager.getIsTextualLogEnabled() && !mqttManager.isTextualLogEnabled)
					{
						coapManager.enableTextualLog();
						mqttManager.isTextualLogEnabled = true;
						System.out.println("Textual log enabled. To disable textual log press 'x'");						
					}
					else
						System.out.println("Error: textual log already enabled");
				}
				
				else if(chuncks[0].equals("x") && chuncks.length == 1)
				{
					if(coapManager.getIsTextualLogEnabled() && mqttManager.isTextualLogEnabled)
					{
						coapManager.disableTextualLog();
						mqttManager.isTextualLogEnabled = false;
						System.out.println("Textual log disabled.");						
					}
					else
						System.out.println("Error: textual log already disabled");
				}

				else if(chuncks[0].equals("disableAutomation") && chuncks.length == 1){
					if(coapManager.getAutomationMode())
						coapManager.disableAutomationMode();
					else
						System.out.println("Error: automation mode already disabled");
				}

				else if(chuncks[0].equals("enableAutomation") && chuncks.length == 1){
					if(!coapManager.getAutomationMode())
						coapManager.enableAutomationMode();
					else
						System.out.println("Error: automation mode already enabled");
				}

				else
					System.out.println("The inserted command does not exist\n");
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void printCommands()
	{
		System.out.println("Commands list:"
				+ "\n\tdisableAutomation 		--> make the actuators control manual"
				+ "\n\tenableAutomation 		--> make the actuators control automatic"
				+ "\n\tsetTemp [value] 			--> set the temperature threshold that determines when turno on/off the temperature actuator(s)"
				+ "\n\tsetLum [value] 		--> set the light threshold that determines when turn on/off the light actuator(s)"
				+ "\n\tgetCurrTemp				--> get the last measured temperature expressed in Celsius degrees"
				+ "\n\tgetCurrLum 				--> get the last measured luminosity expressed in lux in the specified room"
				+ "\n\tgetLumThreshold 			--> get the last light threshold value set"
				+ "\n\tgetTemperatureThreshold 	--> get the last light threshold value set"
				+ "\n\tshowTextLog 				--> data received from sensors are shown as textual log"
				+ "\n\tquit 					--> quit the program");
	}
	
	public static void CoAPInitialization()
	{
		coapManager = new CoAPManager();
	}

	
	public static void MqttInitialization() {
		try 
		{
			mqttManager = new MQTTManager(coapManager);
		} catch(MqttException me) 
		{
			me.printStackTrace();
		}
	}
	
	public static void welcomePrint() 
	{
		System.out.println("************************************************************");
		System.out.println("*********     SMART HOME TELEMETRY APPLICATION     *********");
		System.out.println("************************************************************");
	}
	
}
