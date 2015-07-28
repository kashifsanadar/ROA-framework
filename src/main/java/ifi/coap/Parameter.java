package ifi.coap;

/**
 * This class represents input/output parameters for GET, PUT, POST, and DELETE operations
 * **/
public class Parameter {
	
	// Private memebers
	
	private String name;
	private String type;
	
	public Parameter()
	{
		name = new String();
		type = new String();
	}
	
	/**
	 * getter and setters
	 * **/
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getType()
	{
		return this.type;
	}
	

}


