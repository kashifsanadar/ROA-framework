package com.tasks;

import ifi.coap.CoapClient;

import org.drools.runtime.process.WorkItem;

import org.drools.runtime.process.WorkItemHandler;

import org.drools.runtime.process.WorkItemManager;


public class AccelerometerServiceWorkItemHandler implements WorkItemHandler {


  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

    // extract parameters
	  
    //String message = (String) workItem.getParameter("Message"); // relax this for now

	// send a CoAP request
	CoapClient coapClient = new CoapClient();
    
    try
    {
    	
    	System.out.println(coapClient.sendCoAPRequest("coap://[aaaa::212:7400:1360:c66b]:5683/sensors/temperature", "GET"));
    	
    } 
    catch(Exception e)
    {
    	System.out.println("Target service not found! Make sure the URL of the target is correct");
    }

    // notify manager that work item has been completed
    manager.completeWorkItem(workItem.getId(), null);

  }
  
  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    // Do nothing, notifications cannot be aborted

  }
  
}
