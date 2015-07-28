package ifi.coap;

import java.io.IOException;

import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.ResponseHandler;

public class AsynchClient implements ResponseHandler{
	
	// Implement the client functionality here
	
	public static void main(String[] args) throws InterruptedException{
		
		AsynchClient client = new AsynchClient();
		client.performSampleRequest();
		
		
	}
	
	public void performSampleRequest() throws InterruptedException{
		
		// Create a new request
		 Request request = new GETRequest();
		 
		 // Specify URI of the target endpoint
		 request.setURI("coap://[aaaa::212:7400:1360:c66b]:5683/test/separate");
		 
		 // register client as response handler
		 request.registerResponseHandler(this);
		 
		 // execute the request
		 
		 try {
			 request.execute();
		 } 
		 catch (IOException e){
			 
			 System.err.println("Failed to execute the request: " + e.getMessage());
			 System.exit(-1);
		 }
		 
		 
		 //Do something else	
		 
		
		 request.enableResponseQueue(true);
		 
		 request.receiveResponse();
		 
		 System.out.println("The Response handler called without RH");
		
	}
	
	 @Override
	 public void handleResponse (Response response){
		 
		 // response received
		 
		 // Do something with response
//		 try{
//			 Thread.sleep(1000);
//		 } catch (Exception e){}	
		 
		 System.out.println("The Response handler called with: " + response.getPayloadString());
		 
	 }

}
