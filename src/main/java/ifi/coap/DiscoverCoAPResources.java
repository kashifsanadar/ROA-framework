package ifi.coap;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ch.ethz.inf.vs.californium.endpoint.resources.Resource;
import ch.ethz.inf.vs.californium.endpoint.resources.RemoteResource;

/*
 * This class is responsible to discover CoAP resources associated with inputed IP addresses
 * */

public class DiscoverCoAPResources {
	
	public static Document wadlDoc = null;

	public static void main(String[] args) {
		String routerIpv6Address = new String("aaaa::212:7400:1360:b04e"); // currently static

		// send Http request to router
		requestRouter(routerIpv6Address);

	}

	/**
	 * Send Http request to router to get list of neighbors - need http protocol
	 * will return the string containing list of IPv6 addresses
	 **/

	public static void requestRouter(String ipv6Address) {

		String strNeighborsList = null; // feed router response - must be http based		

		if (!ipv6Address.isEmpty()) {
			
			
			try 
			{
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				
			    // create a temporary file to store router response
			    createFile ((new String (AppachiHttpClient.sendHttpRequest("http://["
						+ ipv6Address + "]/.wellknown/core"))));
			    
			    wadlDoc = docBuilder.parse ("/home/kashifd/jbpm_workspace/WADLs/temp.html");
			  }
		
			catch (ParserConfigurationException e) {System.out.println("ParserConfigurationException");}
			catch (SAXException e) {System.out.println("SAXException");}
			catch (IOException e) {System.out.println("IOException");}
			
			// get the list of neighbors' IP addresses
			NodeList nList = wadlDoc.getElementsByTagName("body");
			
			Node nNode = nList.item(0);
			Element eElement = (Element) nNode;
			
			for (int i = 0; i < nList.getLength(); i++) {
				 
				nNode = nList.item(i);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					eElement = (Element) nNode;
					
					strNeighborsList = eElement.getLastChild().getTextContent();
				}
			}
			
			// send CoAP request to individual neighbor
			if (!strNeighborsList.isEmpty())
				sendCoAPRequestToIndividualNeighbor(strNeighborsList);
			else
				System.out.println("No neighbor discovered from" + ipv6Address);
		}
	}

/**
 * To each neighbor discovered, send Erbium CoAP 13 request 
 * to "/.well-known/core"
 * default port number is 5683 '
 * generates WADL file for each CoAP response
 * **/
	
	public static void sendCoAPRequestToIndividualNeighbor(String strNeighborsList) 

	{
		// Some variables to save neighbors' IP address
		int strtIndex = strNeighborsList.indexOf("aaaa::", 0), // index of first IP address
		endIndex = strtIndex + 24, // The length of IPv6 address
		dstBeginneighborIPAddressInex = 0; // start index of neighborsIPAddresses

		char[] neighborIPAddress = new char[24]; // store individual IP addresses
		String baseURI = new String();
		String linkFormat = new String();

		// Use Californium CoAP client
		CoapClient coapClient = new CoapClient();
	
		// Separate IP addresses from neighborsList	
		while (true)
			{
				strNeighborsList.getChars(strtIndex, endIndex,
				neighborIPAddress, dstBeginneighborIPAddressInex);
				
				baseURI = "coap://[" + new String(neighborIPAddress) + "]:5683"; 
				
				try {
					
					// make a CoAP request
				    linkFormat = coapClient.sendCoAPRequest(baseURI + "/.well-known/core", "GET");
				    
				    //create resource tree from link format
					Resource root = RemoteResource.newRoot(linkFormat);
					
					if (root != null) 
					{
						// get complete wadl file
						wadlDoc = root.getWADLDoc(baseURI); 
						// save the wadl file
						saveWADL(wadlDoc, new String (neighborIPAddress));
					} 
	
				} catch (ParserConfigurationException pce) {
					pce.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				// get next neighbor's IP address
				strtIndex = strNeighborsList.indexOf("aaaa::", endIndex); // index of next IP address
				if (strtIndex == -1)
					break; // job done - the whole list is parsed
				endIndex = strtIndex + 24; // length of IP address
	
			} // end while
}
/**
 * Save WADL document into an XML file
 * **/
	
	public static void saveWADL(Document wadlDoc, String fileName) {

		try {

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(wadlDoc);

			StreamResult result = new StreamResult(new File("/home/kashifd/jbpm_workspace/WADLs/wadl " + fileName + ".xml"));

			transformer.transform(source, result);

			System.out.println("File saved with name: wadl " + fileName + ".xml");
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
/**
 * construct DOM document out of well structured and normalized string
 * 
 * **/
	
	public static Document getDocument(String strHtml)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		
		Document doc = null;
		try {
		    db = dbf.newDocumentBuilder();
		    InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(strHtml));
		    try {
		        doc= db.parse(is);
		        
		    } catch (SAXException e) {
		        // handle SAXException
		    } catch (IOException e) {
		        // handle IOException
		    }
		} catch (ParserConfigurationException e1) {
		    // handle ParserConfigurationException
		}
		
		return doc;
	}
	
/**
 * create a file
 * **/
	
	public static void createFile(String strFileContents) {
		
		FileWriter ryt;
		try {
			ryt = new FileWriter("/home/kashifd/jbpm_workspace/WADLs/temp.xml");
			BufferedWriter out=new BufferedWriter(ryt);
			out.write(strFileContents);
			out.close();
			
			System.out.println(" Temporary file created");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
