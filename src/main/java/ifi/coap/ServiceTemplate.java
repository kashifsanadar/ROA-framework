package ifi.coap;

	/**
/**
/**
 * Auto generated Temperature (resource) class that extends Resource
 *

public class Temperature extends Resource
{
	// Private members - set of input and output parameters
	
	// Private variable of type CoapClient to make CoAP requests
	CoapClient coapClient = new CoapClient();
	
	// Sample method with input and output parameters
	
	public [Output parameter return type] Method_Name (List of input parameters)
	{
		// instantiate CoapClient and make a CoAP request
		// Process the CoAP response and return the result
	}
	
	// Public GET, PUT, POST, and DELETE methods	
}
**/

public class ServiceTemplate

{
	public static void main(String args[])
	{
		Parameter[] para_in = new Parameter[2];
		
		for (int i = 0; i < para_in.length; i++) para_in[i] = new Parameter();
		
		para_in[0].setName("para1"); para_in[0].setType("String");
		para_in[1].setName("para2"); para_in[1].setType("Integer");
		
		Parameter para_out = new Parameter();
		para_out.setName("para_out"); para_out.setType("double");
		
		ServiceTemplate srvTemplate = new ServiceTemplate("Temperature", "base_uri", "resource_path");
		srvTemplate.insertMethod("GET", para_in, para_out);
		
		System.out.println(srvTemplate.getServiceTemplate());
		
	}
	private String strServiceTemplate; //represents service template 
	
	/**
	 * get auto-generated template for service 
	 * **/
	public String getServiceTemplate()
	{
		return strServiceTemplate += "}"; 
	} 
	
	/**
	 * construct initial service template with default constructor 
	 * **/
	public ServiceTemplate(String className, String baseURI, String resourcePath)
		{
			strServiceTemplate = new String();
			
			strServiceTemplate += "/**\n" +
								  "* Auto generated " + className + " (resource) class that extends Resource class\n" +
								  "**/\n\n" +
								  "public class " + className +  " extends Resource\n" +
								  "{\n" +
								  
								  "\t//Declare private members here\n\n" +
					
								  "\t// through default constructor, set resource full path\n" + 
			  					  "\tpublic " + className +"() {\n" + 
			  					  "\t\tsuper(" + baseURI +", " + resourcePath + ");\n\t}\n" +
			  					  "\t// instantiate CoapClient to make a CoAP request\n" +
								  "\tCoapClient coapClient = new CoapClient();\n\n";
		} 

		
		/**
		 * Insert method
		 * can be GET, PUT, POST, or DELETE method
		 * **/
		public void insertMethod(String methodType, Parameter[] inputParams, Parameter outputParams) 
		{ 
			strServiceTemplate += "\tpublic " + outputParams.getType() + " perform" + methodType + "(";
			
			// append the list of parameters
			for (int i = 0; i < inputParams.length; i++)
			{
				strServiceTemplate += inputParams[i].getType() + " " + inputParams[i].getName();
				
				if (i < (inputParams.length -1))
					
					// go and append next parameter
					strServiceTemplate += ", ";
				else
					// close the parenthesis and start method definition
					
					strServiceTemplate += ") {\n\n";
			}
			
			/** method definition **/
			
			strServiceTemplate += "\t// Local vaiable(s)\n" +
								  "\tString coapResponse;\n" +
								  "\t" + outputParams.getType() + " result;\n\n" +
								  "\t//TODO: Before making CoAP request, formulate the CoAP request based on the input parameters\n\n" +
								  "\t//make CoAP request\n" + 
								  "\tcoapResponse = this.coapClient.sendCoAPRequest( super.getCompletePath(), " +  methodType +")\n\n" +
								  
								  "\t//TODO: Process coapResponse to get result\n\n" +
								  "\treturn result;\n}\n";
		}
}
