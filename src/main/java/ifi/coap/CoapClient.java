package ifi.coap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;

import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.TokenManager;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.endpoint.resources.RemoteResource;
import ch.ethz.inf.vs.californium.endpoint.resources.Resource;
import ch.ethz.inf.vs.californium.util.Log;

/**
 * This class implements a CoAP client for testing purposes. Usage:
 * <p>
 * {@code java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]}
 * <ul>
 * <li>METHOD: {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}
 * <li>URI: The URI to the remote endpoint or resource}
 * <li>PAYLOAD: The data to send with the request}
 * </ul>
 * Options:
 * <ul>
 * <li>-l: Loop for multiple responses}
 * </ul>
 * Examples:
 * <ul>
 * <li>{@code SampleClient DISCOVER coap://localhost}
 * <li>{@code SampleClient POST coap://someServer.org:5683 my data}
 * </ul>
 * 
 * @author Dominique Im Obersteg, Daniel Pauli, and Matthias Kovatsch
 */

public class CoapClient {

	// resource URI path used for discovery
	private static final String DISCOVERY_RESOURCE = "/.well-known/core";

	// exit codes for runtime errors
	private static final int ERR_MISSING_METHOD = 1;
	private static final int ERR_UNKNOWN_METHOD = 2;
	//private static final int ERR_MISSING_URI = 3; // relax this for now
	private static final int ERR_BAD_URI = 4;
	private static final int ERR_REQUEST_FAILED = 5;
	private static final int ERR_RESPONSE_FAILED = 6;
	private static final int ERR_BAD_LINK_FORMAT = 7;
	
	/**
	// indices of command line parameters
	private static final int IDX_METHOD = 0;
	private static final int IDX_URI = 1;
	private static final int IDX_PAYLOAD = 2;
	**/

	/*
	 * Make CoAP requests from this client.
	 */

	public String sendCoAPRequest(String CoAPURL, String method) throws Exception {

		// initialize parameters
		URI uri = new URI(CoAPURL);
		String payload = null;
		boolean loop = false;

		// display help if no parameters specified
		/*
		 * if (args.length == 0) { printInfo(); return; }
		 */

		Log.setLevel(Level.ALL);
		Log.init();

		/*
		 * for (String arg : args) { if (arg.startsWith("-")) { if
		 * (arg.equals("-l")) { loop = true; } else {
		 * System.out.println("Unrecognized option: " + arg); } } else { switch
		 * (idx) { case IDX_METHOD: method = arg.toUpperCase(); break; case
		 * IDX_URI: try { uri = new URI(arg); } catch (URISyntaxException e) {
		 * System.err.println("Failed to parse URI: " + e.getMessage());
		 * System.exit(ERR_BAD_URI); } break; case IDX_PAYLOAD: ' payload = arg;
		 * break; default: System.out.println("Unexpected argument: " + arg); }
		 * '' ++idx; } }
		 */

		// check if mandatory parameters specified
		if (method == null) {
			System.err.println("Method not specified because I didn't have mood to specify it!");
			System.exit(ERR_MISSING_METHOD);
		}
/**
 * Relax this for now 
 * 
 
			if (uri == null) {
			System.err.println("URI not specified");
			System.exit(ERR_MISSING_URI);
		}
*
**/
		// create request according to specified method
		Request request = newRequest(method);
		if (request == null) {
			System.err.println("Unknown method, why?!: " + method);
			System.exit(ERR_UNKNOWN_METHOD);
		}

		if (method.equals("OBSERVE")) {
			request.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
			loop = true;
		}

		// set request URI
		if (method.equals("DISCOVER")
				&& (uri.getPath() == null || uri.getPath().isEmpty() || uri
						.getPath().equals("/"))) {
			// add discovery resource path to URI
			try {
				uri = new URI(uri.getScheme(), uri.getAuthority(),
						DISCOVERY_RESOURCE, uri.getQuery());

			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI: " + e.getMessage());
				System.exit(ERR_BAD_URI);
			}
		}

		request.setURI(uri);
		request.setPayload(payload);
		request.setToken(TokenManager.getInstance().acquireToken());
		request.setContentType(MediaTypeRegistry.TEXT_PLAIN);

		// enable response queue in order to use blocking I/O
		request.enableResponseQueue(true);

		//
		request.prettyPrint();

		// execute request
		try {
			request.execute();

			// loop for receiving multiple responses
			do {

				// receive response

				System.out.println("Receiving response...");
				Response response = null;
				try {
					response = request.receiveResponse();
				} catch (InterruptedException e) {
					System.err.println("Failed to receive response!"
							+ e.getMessage());
					System.exit(ERR_RESPONSE_FAILED);
				}

				// output response

				if (response != null) {

					response.prettyPrint();
					System.out.println("Time elapsed (ms): "
							+ response.getRTT());

					// check of response contains resources
					if (response.getContentType() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {

						String linkFormat = response.getPayloadString();

						// create resource three from link format
						Resource root = RemoteResource.newRoot(linkFormat);
						if (root != null) {

							// output discovered resources
							System.out.println("\nDiscovered resources:");
							root.prettyPrint();

						} else {
							System.err.println("Failed to parse link format");
							System.exit(ERR_BAD_LINK_FORMAT);
						}
					} else {

						// check if link format was expected by client
						if (method.equals("DISCOVER")) {
							System.out
									.println("Server error: Link format not specified!");
						}
					}
					return response.getPayloadString();

				} else {

					// no response received
					System.err.println("Request timed out!");
					break;
				}

			} while (loop);

		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
			System.exit(ERR_REQUEST_FAILED);
		} catch (IOException e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(ERR_REQUEST_FAILED);
		}

		// finish
		System.out.println();

		return "Error";
	}

	/*
	 * Outputs user guide of this program.
	 */
	public static void printInfo() {
		System.out.println("Californium (Cf) Client");
		System.out
				.println("(c) 2012, Institute for Pervasive Computing, ETH Zurich");
		System.out.println();
		System.out.println("Usage: " + CoapClient.class.getSimpleName()
				+ " [-l] METHOD URI [PAYLOAD]");
		System.out
				.println("  METHOD  : {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}");
		System.out
				.println("  URI     : The CoAP URI of the remote endpoint or resource");
		System.out.println("  PAYLOAD : The data to send with the request");
		System.out.println("Options:");
		System.out.println("  -l      : Loop for multiple responses");
		System.out
				.println("           (automatic for OBSERVE and separate responses)");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  ExampleClient DISCOVER coap://localhost");
		System.out
				.println("  ExampleClient POST coap://vs0.inf.ethz.ch:5683/storage my data");
	}

	/*
	 * Instantiates a new request based on a string describing a method.
	 * 
	 * @return A new request object, or null if method not recognized
	 */
	private static Request newRequest(String method) {
		if (method.equals("GET")) {
			return new GETRequest();
		} else if (method.equals("POST")) {
			return new POSTRequest();
		} else if (method.equals("PUT")) {
			return new PUTRequest();
		} else if (method.equals("DELETE")) {
			return new DELETERequest();
		} else if (method.equals("DISCOVER")) {
			return new GETRequest();
		} else if (method.equals("OBSERVE")) {
			return new GETRequest();
		} else {
			return null;
		}
	}

}
