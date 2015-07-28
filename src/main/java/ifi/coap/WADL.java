package ifi.coap;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 *
 <application xmlns="http://wadl.dev.java.net/2009/02">
 <resources base="coap://[Ipv6 address of router]/:port_number">
 <resource path="/temperature">
 <method name="GET"/>
 <response representation mediaType="application/xml">
 </resource>        
 <resource path="/light">
 <method name="GET"/>
 <response representation mediaType="application/xml">
 </resource>	
 <resource path="/led">
 <method name="PUT">
 <request>
 <param name="color" required="false" default="green" style="query"/>
 </request>
 </method>
 </resource>
 </resources>
 </application>
 * 
 * */

public class WADL {

	public static void main(String args[]) {

		Document wadlDoc = createWADL();
		saveWADL(wadlDoc);

	}

	/* default constructor */

	public static Document createWADL() {

		Document wadlDoc = null;
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			wadlDoc = docBuilder.newDocument();
			Element rootElement = wadlDoc.createElement("application");
			wadlDoc.appendChild(rootElement);

			// resources element(s) - next to "application"
			Element resources = wadlDoc.createElement("resources");
			rootElement.appendChild(resources);

			// set attribute to resources element
			resources.setAttribute("id", "1");
			resources.setAttribute("base_uri", "Some URI");

			// add resource to resources - later
			wadlDoc = addResource(wadlDoc, resources, "/sensors/light", "GET");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		return wadlDoc;

	}

	public static void saveWADL(Document wadlDoc) {

		try {

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(wadlDoc);

			StreamResult result = new StreamResult(new File(
					"/home/kashifd/jbpm_workspace/WADLs/file.xml"));

			transformer.transform(source, result);

			System.out.println("File saved!");
		}

		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}

	}

	// add resource to resources - with request and response

	public static Document addResource(Document wadlDoc, Element resources,
			String path, String methodName) // interface description leading to
											// the URI of Interface
											// note - a resource can implement
											// multiple interfaces each
											// separated by space
	{
		Element resource = wadlDoc.createElement("resource");
		resource.setAttribute("path", path);

		// add interface to resource - provides the URI to the directory where
		// interface is described
		Element method = wadlDoc.createElement("method");
		method.setAttribute("name", methodName);

		// TODO: We can further add in/out parameters to the later

		// append interface to resource
		resource.appendChild(method);

		// append resource to resources
		resources.appendChild(resource);

		return wadlDoc;

	} // end addResource(..)

} // end WADL class

