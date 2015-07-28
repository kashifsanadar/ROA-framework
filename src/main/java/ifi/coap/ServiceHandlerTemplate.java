package ifi.coap;

/**
 * This class provides a template for automatically generating Service Handler (SH)file
 * SH provides an API to make CoAP requests to IoT services  
 * **/

public class ServiceHandlerTemplate {
	
	private static String strServiceHandler;
	
	public ServiceHandlerTemplate() 
	{
		strServiceHandler = new String();
	}
	
	public static String createServiceHandler(String packageName, // default name is 'com.tasks' 
											  String className, // Currently equal to 'rt' attribute 
											  					//Class name of the service handler must be combination of IoT device name + 
											  				   //ServiceWorkItemHandler e.g. AccelerometerServiceWorkItemHandler
											  Parameter[] inputParam,  // Input parameters
											  Parameter outputParam) // Output parameter
											
	{
		strServiceHandler = "package " + packageName + ";\n\n" +
							"import ifi.coap.CoapClient;\n" +
							"import org.drools.runtime.process.WorkItem;\n" +
							"import org.drools.runtime.process.WorkItemHandler;\n" +
							"import org.drools.runtime.process.WorkItemManager;\n\n" +
				
							"public class "  + className + "ServiceWorkItemHandler implements WorkItemHandler {\n\n" +
							"\tpublic void executeWorkItem(WorkItem workItem, WorkItemManager manager) {\n\n" +
							"\t// Map local parameters\n";
		
		// declare local variables for input parameters
		for (int i = 0; i < inputParam.length; i++) 
			strServiceHandler += "\t" + inputParam[i].getType() + " " + inputParam[i].getName() + " = " + 
								 "workItem.getParameter(\"" + inputParam[i].getName() + "\");\n";
		// for output parameters
		strServiceHandler += "\t" + outputParam.getType() + " " + outputParam.getName() + ";\n";
		
		// instantiate service class
		strServiceHandler += "\t" + className + " instanceOf" + className + " = new " + className + "();\n\n" +
							 "\t// Use \"instanceOf" + className + "\" to invoke " + className + " resource\n\n\n" +
								 
							 "\t// notify manager that work item has been completed\n" +
							 "\tmanager.completeWorkItem(workItem.getId(), null);\n\n" +
							 "\t} //End executeWorkItem\n\n" +
							 "\tpublic void abortWorkItem(WorkItem workItem, WorkItemManager manager) {\n\n" +
							 "\t// Do nothing, notifications cannot be aborted\n\n" +
							 "\t} // End abortWorkItem \n" +
							 "} // End class ServiceHandlerTemplate"; 
	
		return strServiceHandler;
	}
}
