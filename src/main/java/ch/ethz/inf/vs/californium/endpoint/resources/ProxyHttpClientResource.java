/**
 * 
 */

package ch.ethz.inf.vs.californium.endpoint.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestDate;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.util.CoapTranslator;
import ch.ethz.inf.vs.californium.util.HttpTranslator;
import ch.ethz.inf.vs.californium.util.InvalidFieldException;
import ch.ethz.inf.vs.californium.util.Properties;
import ch.ethz.inf.vs.californium.util.TranslationException;

/**
 * // test with http://httpbin.org/
 * 
 * @author Francesco Corazza
 * 
 */
public class ProxyHttpClientResource extends ForwardingResource {

	private static final int KEEP_ALIVE = Properties.std.getInt("HTTP_CLIENT_KEEP_ALIVE");
	/**
	 * DefaultHttpClient is thread safe. It is recommended that the same
	 * instance of this class is reused for multiple request executions.
	 */
	private static final AbstractHttpClient HTTP_CLIENT = new DefaultHttpClient();

	// http client static configuration
	static {
		// request interceptors
		HTTP_CLIENT.addRequestInterceptor(new RequestAcceptEncoding());
		HTTP_CLIENT.addRequestInterceptor(new RequestConnControl());
		// HTTP_CLIENT.addRequestInterceptor(new RequestContent());
		HTTP_CLIENT.addRequestInterceptor(new RequestDate());
		HTTP_CLIENT.addRequestInterceptor(new RequestExpectContinue());
		HTTP_CLIENT.addRequestInterceptor(new RequestTargetHost());
		HTTP_CLIENT.addRequestInterceptor(new RequestUserAgent());

		// response intercptors
		HTTP_CLIENT.addResponseInterceptor(new ResponseContentEncoding());

		HTTP_CLIENT.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					// Keep connections alive if a keep-alive value
					// has not be explicitly set by the server
					keepAlive = KEEP_ALIVE;
				}
				return keepAlive;
			}

		});
	}

	public ProxyHttpClientResource() {
		// set the resource hidden
		super("proxy/httpClient", true);
		setTitle("Forward the requests to a HTTP client.");
	}

	@Override
	public Response forwardRequest(final Request incomingCoapRequest) {
		// check the invariant: the request must have the proxy-uri set
		if (!incomingCoapRequest.hasOption(OptionNumberRegistry.PROXY_URI)) {
			LOG.warning("Proxy-uri option not set.");
			return new Response(CodeRegistry.RESP_BAD_OPTION);
		}

		// remove the fake uri-path
		incomingCoapRequest.removeOptions(OptionNumberRegistry.URI_PATH); // HACK

		// get the proxy-uri set in the incoming coap request
		URI proxyUri;
		try {
			proxyUri = incomingCoapRequest.getProxyUri();
		} catch (URISyntaxException e) {
			LOG.warning("Proxy-uri option malformed: " + e.getMessage());
			return new Response(CoapTranslator.STATUS_FIELD_MALFORMED);
		}

		// get the requested host, if the port is not specified, the constructor
		// sets it to -1
		HttpHost httpHost = new HttpHost(proxyUri.getHost(), proxyUri.getPort(), proxyUri.getScheme());

		HttpRequest httpRequest = null;
		try {
			// get the mapping to http for the incoming coap request
			httpRequest = HttpTranslator.getHttpRequest(incomingCoapRequest);
			LOG.finer("Outgoing http request: " + httpRequest.getRequestLine());
		} catch (InvalidFieldException e) {
			LOG.warning("Problems during the http/coap translation: " + e.getMessage());
			return new Response(CoapTranslator.STATUS_FIELD_MALFORMED);
		} catch (TranslationException e) {
			LOG.warning("Problems during the http/coap translation: " + e.getMessage());
			return new Response(CoapTranslator.STATUS_TRANSLATION_ERROR);
		}

		ResponseHandler<Response> httpResponseHandler = new ResponseHandler<Response>() {
			@Override
			public Response handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
				long timestamp = System.nanoTime();
				LOG.finer("Incoming http response: " + httpResponse.getStatusLine());
				// the entity of the response, if non repeatable, could be
				// consumed only one time, so do not debug it!
				// System.out.println(EntityUtils.toString(httpResponse.getEntity()));

				// translate the received http response in a coap response
				try {
					Response coapResponse = HttpTranslator.getCoapResponse(httpResponse, incomingCoapRequest);
					coapResponse.setTimestamp(timestamp);
					return coapResponse;
				} catch (InvalidFieldException e) {
					LOG.warning("Problems during the http/coap translation: " + e.getMessage());
					return new Response(CoapTranslator.STATUS_FIELD_MALFORMED);
				} catch (TranslationException e) {
					LOG.warning("Problems during the http/coap translation: " + e.getMessage());
					return new Response(CoapTranslator.STATUS_TRANSLATION_ERROR);
				}
			}
		};

		// accept the request sending a separate response to avoid the timeout
		// in the requesting client
		incomingCoapRequest.accept();
		LOG.finer("Acknowledge message sent");

		Response coapResponse = null;
		try {
			// execute the request
			coapResponse = HTTP_CLIENT.execute(httpHost, httpRequest, httpResponseHandler, null);
		} catch (IOException e) {
			LOG.warning("Failed to get the http response: " + e.getMessage());
			return new Response(CodeRegistry.RESP_INTERNAL_SERVER_ERROR);
		}

		return coapResponse;
	}
}
