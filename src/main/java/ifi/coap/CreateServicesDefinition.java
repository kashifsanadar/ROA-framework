package ifi.coap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import ch.ethz.inf.vs.californium.coap.LinkFormat;

import java.io.File;
import java.io.BufferedWriter;
//import java.io.FileNotFoundException; //TODO: handel this exception
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible to:
 * (i) - parse the xml file using document of type org.w3c.dom.Document
 * (ii) - automatically generate Services definition file for a given WADL files
 * 
 * Structure of the WADL file
 * 
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <application>
 * 				<resources>
 * 							<resource>
 * 										<methods>
 * 												 <method>
 * 												  		<input_param name> </input_param>
 * 														<output_param> </output_param> 
 * 												 
 * 												 </method>
 * 										</methods>
 * 							</resource>
 * 				</resources>
 * </application>
 * 
 * 
 * **/

public class CreateServicesDefinition {
	
	//public static void createServicesDefinition() {
	
	public static void main(String argv[]) {
		
		NodeList rsList, rList, mList, pList; // // represents resource and method lists
		Node rsNode, rNode, mNode, pNode; // represents resource and method nodes
		Element rsElement, rElement, mElement, pElement; // method element
		
		int numOfWADLs = 5; // Number of wadl files to parse, suppose 5
		ServiceDefTemplate serviceDefTemplate = new ServiceDefTemplate();
		
		Document wadlDoc = null;
		Document bigWADLDoc = null;
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			bigWADLDoc = dBuilder.newDocument(); 
			Element rootElement = bigWADLDoc.createElement("Applications"); // create a root node
			bigWADLDoc.appendChild(rootElement);
			wadlDoc = dBuilder.newDocument();
			
			for ( int i = 0; i <= numOfWADLs; i++)
			{
				//first load wadl file into DOM document e.g., 
				File fXmlFile = new File("/home/kashifd/jbpm_workspace/WADLs/wadl aaaa::212:7400:1360:c66b.xml");
				wadlDoc = dBuilder.parse(fXmlFile);
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				wadlDoc.getDocumentElement().normalize();
				
				// merge wadlDoc with bigWADLDoc document
				Element iElement = (Element) wadlDoc.getDocumentElement();	
				Node imported = bigWADLDoc.importNode(iElement, true);				 
				rootElement.appendChild(imported);	
			}
		} 
		
		catch (Exception e) {e.printStackTrace();}
			
			// get nodes list under 'resources'
			rsList = bigWADLDoc.getElementsByTagName("resources");
			
			for ( int i = 0; i < rsList.getLength(); i++)
			{
				// pick up the first node
				rsNode = rsList.item(i);
				rsElement = (Element) rsNode;
				
				//System.out.println("\nCurrent Resources available at : " + rsElement.getAttribute("base_uri"));
				
				// get list of resource elements 
				// note - only single 'resources' node
				rList = rsElement.getElementsByTagName("resource"); 
				
				for (int j = 0; j < rList.getLength(); j++) {
					 
					rNode = rList.item(j);
					rElement = (Element) rNode;
					
					// create service template
					ServiceTemplate serviceTemplate = new ServiceTemplate(rElement.getAttribute("rt"), 
																		  rsElement.getAttribute("base_uri"),
																		  rElement.getAttribute("path"));
							
					// access list of nodes under resource -> methods -> method -> (target e.g. input_param)
					mList = ((Element) rNode.getFirstChild()).getElementsByTagName("method");
					
					for (int k = 0; k < mList.getLength(); k++) 
					{
						// parse each method node
						mNode = mList.item(k);
						mElement = (Element) mNode;

						// access method attributes here via mElement
						serviceDefTemplate.insertSDServiceName(rElement.getAttribute("rt") + "_" + mElement.getAttribute("name"));

						/** access parameters attributes here via pElement **/
						
						// input parameter
						
						pList = ((Element) mNode).getElementsByTagName("input_param");
						Parameter[] inputParams = new Parameter[pList.getLength()];
						
						for (int l = 0; l < pList.getLength(); l++) 
						{
							pNode = pList.item(l);
							pElement = (Element) pNode;
							inputParams[l] = new Parameter();
							inputParams[l].setName(pElement.getAttribute("name")); 
							inputParams[l].setType(pElement.getAttribute("type"));
							
							serviceDefTemplate.insertSDInputParameter(inputParams[l].getName(), inputParams[l].getType(), (pList.getLength()-1));
						}
						
						// output parameter - only one output parameter
						Parameter outputParam = new Parameter();
						pList = ((Element) mNode).getElementsByTagName("output_param");
						
						pNode = pList.item(0); // only one output parameter
						pElement = (Element) pNode; 
						outputParam.setName(pElement.getAttribute("name"));
						outputParam.setType(pElement.getAttribute("type"));
						
						// append to service definition
						serviceDefTemplate.insertSDOutputParameter(outputParam.getName(), outputParam.getType());
						// append to service template
						serviceTemplate.insertMethod(mElement.getAttribute("name"), inputParams, outputParam);
						
												
						// insert display name and icon path
						if (i < (rsList.getLength() -1))
							serviceDefTemplate.insertSDDisplayNameWithIcon(rElement.getAttribute("rt"), "icon path");
						else
						
						serviceDefTemplate.insertSDDisplayNameWithIcon(rElement.getAttribute("rt"), "icon path", mList.getLength());
						
						// create service handler class for the resource
						createFile(ServiceHandlerTemplate.
														  createServiceHandler("ifi.coap", 
																			   rElement.getAttribute("rt"), 
																	           inputParams, 
																	           outputParam), 
														    rElement.getAttribute("rt") + ".java");
							
					} // end method tag
					
					// write service template
					createFile(serviceTemplate.getServiceTemplate(), (rElement.getAttribute("rt") +  ".java"));
				}	// end resource tag
			} // end resources tag
			
			// create Service Definition file
			createFile(serviceDefTemplate.getServiceDefinition(), "sd.wid"); // create SD file
		
	}// end method createServicesDefinition 
	
	public static void createFile(String strFileContents, String fileName) {
		
		FileWriter ryt;
		try {
			ryt = new FileWriter("/home/kashifd/jbpm_workspace/Services Artifacts/" + fileName);
			BufferedWriter out=new BufferedWriter(ryt);
			out.write(strFileContents);
			out.close();
			
			System.out.println("File Saved");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    }
	

	} // end class CreateServicesDefinition
