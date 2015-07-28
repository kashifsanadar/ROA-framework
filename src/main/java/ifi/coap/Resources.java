package ifi.coap;

/**
 * This class represents (IoT) Resources that can be extended by each IoT resource that exposes REST interface 
 * **/
public class Resources 
{
	public String baseURI;
	
	/**
	 * default constructor
	 * **/
	public Resources()
	{
		baseURI = new String();
	}

	/**
	 * constructor - set base URI
	 * **/
	public  Resources(String baseURI)
	{
		this.baseURI = baseURI;
	} 	
	/**
	 * set base URI
	 * **/
	
	public  void setBaseURI(String baseURI)
	{
		this.baseURI = baseURI;
	} 
	public  String getBaseURI()
	{
		return this.baseURI;
	} 
}
