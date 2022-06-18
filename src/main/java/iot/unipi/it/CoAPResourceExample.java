package iot.unipi.it;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import org.eclipse.californium.core.CoapResource;

public class CoAPResourceExample extends CoapResource {

	private InetAddress sourceAddr;
	private int sourcePort;

	public CoAPResourceExample(String name) {
		super(name);
		setObservable(true);
 	}
 	public void handleGET(CoapExchange exchange) {
 		
 		Response response = new Response(ResponseCode.CONTENT);
 		
 		if( exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_XML ) {
 			response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_XML);
 			response.setPayload("<value>10</value>");
 		} else if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
 			response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
 			response.setPayload("{\"value\":\"10\"}"); 			
 		} else {
 			response.setPayload("Hello");
 		}
 		
		exchange.respond(response);
 	}
	public void handlePOST(CoapExchange exchange) {

		System.out.println("Inside HandlePost");
		InetAddress sourceAddr;
		int sourcePort;
		String ACK = "NotConfirmed";
		
		byte[] request = exchange.getRequestPayload();
		
		String s = new String(request);
		System.out.println(s);

        if(s.equals("Temperature")){
			this.setSourceAddr(exchange.getSourceAddress());
			this.setSourcePort(exchange.getSourcePort());
			CoAPManager.addNewTemperatureActuator(exchange.getSourceAddress(), exchange.getSourcePort() );
			ACK = "Confirmed";
			System.out.println("After Registration");
		}

		else if(s.equals("Light")){
			this.setSourceAddr(exchange.getSourceAddress());
			this.setSourcePort(exchange.getSourcePort());
			CoAPManager.addNewLightActuator(exchange.getSourceAddress(), exchange.getSourcePort() );
			ACK = "Confirmed";
			System.out.println("After Registration");
		}
				
		/*Integer value = Integer.parseInt(s);
		
		Double resp = Math.sqrt(value);
		
		s=Double.toString(resp);*/
		
		Response response = new Response(ResponseCode.CONTENT);
		
		response.setPayload(ACK);
				
		exchange.respond(response);
 	}

	public void setSourceAddr(InetAddress newSourceAddr){
		this.sourceAddr = newSourceAddr;
	}
	public void setSourcePort(int newSourcePort){
		this.sourcePort = newSourcePort;
	}

	public InetAddr getSourceAddr(){
		return this.sourceAddr;
	}
	public int getSourcePort(){
		return this.sourcePort;
	}
}