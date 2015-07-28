package ifi.coap;

/**
 * This class (provided by Appachi) is an Http Client class 
 * **/

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;

public class AppachiHttpClient {

  //private static String url = "http://[aaaa::212:7400:1360:b04e]/";

  public static void main(String[] args)
  {
	  for (int i = 0; i < 10; i++) {
		  
		  long before = System.currentTimeMillis();
			 
		  System.out.println(new String (sendHttpRequest("http://[aaaa::212:7400:1360:c66b]:8080/light")));
		  System.out.println("Response time : " + (System.currentTimeMillis() - before));
	  }
	  
  }
	
	public static String sendHttpRequest(String url) {
    
	// Create an instance of HttpClient.
    HttpClient client = new HttpClient();

    // Create a method instance.
    GetMethod method = new GetMethod(url);

    // Provide custom retry handler is necessary
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        	new DefaultHttpMethodRetryHandler(3, false));

    byte[] responseBody = null;
    try {
      // Execute the method.
      int statusCode = client.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + method.getStatusLine());
      }

      // Read the response body.
      responseBody = method.getResponseBody();
     

    } catch (HttpException e) {
      System.err.println("Fatal protocol violation: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Fatal transport error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
    
    // Deal with the response.
    // Use caution: ensure correct character encoding and is not binary data
    
     return new String(responseBody);
  }
}