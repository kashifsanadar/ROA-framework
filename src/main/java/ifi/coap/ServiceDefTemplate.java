package ifi.coap;

/**
 * This class provides a template for Service Definition file
 * **/

/** Sample Service Definition (SD) file - 
 * 
import org.drools.process.core.datatype.impl.type.StringDataType;
[
  // the Notification work item
  [
    "name" : "Accelerometer",
    "parameters" : [
    "Message" : new StringDataType(),
    ],
    "displayName" : "Accelerometer",
    "icon" : "icons/icon.png"
  ]
]
 * **/

public class ServiceDefTemplate {
	
	static int inputParamIndex = 0;
	static int outputParamIndex = 0;
	static int resourceIndex = 0;
	
	public ServiceDefTemplate()
	{
		strServicesDefinition = new String();
		
		strServicesDefinition += "import org.drools.process.core.datatype.impl.type.StringDataType;\n\n" +
		"[\n" +
		"  // the Notification work item\n\n";
	} 
	
	private String strServicesDefinition;
	
	public String getServiceDefinition()
	{
		return strServicesDefinition += "]"; // end SD file 
	} 
	
	/**
	 * Insert service name
	 * current equal to resource type
	 * **/
	public void insertSDServiceName(String resourceName) 
	{ 
		strServicesDefinition += "  [\n" +
								 "    \"name\" :\"" + resourceName + "\"" + ",\n\n";
	}
	
	/**
	 * Insert input parameters
	 * **/
	
	public void insertSDInputParameter(String paramName, String type, int numOfParams)
	{
		if (inputParamIndex == 0) // first parameter
			strServicesDefinition += "    \"parameters\" : [\n";
		
		strServicesDefinition +=  "    \"" + paramName + "\":" + " new " + type + "DataType()";
		
		if (inputParamIndex < numOfParams) // intermediate parameter
			strServicesDefinition += ",\n";
		else
			strServicesDefinition += "\n";
		
		if(inputParamIndex == numOfParams) // last input parameter
		{
			strServicesDefinition += "    ],\n";
			inputParamIndex = 0; //reset inputParamIndex
			return;
		}
		
		++inputParamIndex;
	}
	
	/**
	 * Insert output parameters
	 * **/
	
	
	public void insertSDOutputParameter(String paramName, String type)
	{
		strServicesDefinition += "    \"result\" : [\n" +
									 "    \"" + paramName + "\":" + " new " + type + "DataType()\n" +
									 "    ],\n";
	}
	
	public void insertSDOutputParameter(String paramName, String type, int numOfParams)
	{
		if (outputParamIndex == 0) // first parameter
		{
			strServicesDefinition += "    \"result\" : [\n";
		}
			
		
		strServicesDefinition +=  "    \"" + paramName + "\":" + " new " + type + "DataType()";
		
		if (outputParamIndex < numOfParams) // intermediate parameter
			strServicesDefinition += ",\n";
		else
			strServicesDefinition += "\n";
		
		if(outputParamIndex == numOfParams) // last input parameter
		{
			strServicesDefinition += "    ],\n";
			outputParamIndex = 0; //reset outputParamIndex
			return;
		}
		
		++outputParamIndex;
	}
	
	/**
	 * Insert display name and icon
	 * 	 * **/
	
	public void insertSDDisplayNameWithIcon(String displayName, String icon)
	{
		strServicesDefinition += "    \"displayName\" :\"" + displayName + "\"" + ",\n" +
		 						 "    \"icon\" : \"" + icon + "\"" + "\n" +
		 						 "  ],\n";
		}
	
	public void insertSDDisplayNameWithIcon(String displayName, String icon, int numOfresources)
	{
		strServicesDefinition += "    \"displayName\" :\"" + displayName + "\"" + ",\n" +
		 "    \"icon\" : \"" + icon + "\"" + "\n";
		
		if (resourceIndex < numOfresources)
		
			strServicesDefinition += "  ],\n";
		
		else
			strServicesDefinition += "  ]\n";
		
		++resourceIndex;		
	}
}