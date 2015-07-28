package ifi.coap;

/**
 * This class represents a resource which is element of the 'resources' tag in DOM xml document
 * **/
public class Resource extends Resources{
	
	private String path;
	
	public Resource()
	{
		super();
		path = new String();
	}
	
	public Resource(String baseURI, String resourcePath)
	{
		super(baseURI);
		this.path = resourcePath;
	}
	
	/**
	 * set complete URI path of resource
	 * **/
	
	public  void setPath(String baseURI, String path)
	{
		super.baseURI = baseURI;
		this.path = path;
	} 
	
	public  String getBaseURI()
	{
		return this.baseURI;
	} 
	public  String getPath()
	{
		return this.path;
	} 
	
	public  String getCompletePath()
	{
		return super.baseURI + this.baseURI;
	} 
	
	/**
	 * Define general GET, PUT, POST, and PUT methods here
	 * **/
}
