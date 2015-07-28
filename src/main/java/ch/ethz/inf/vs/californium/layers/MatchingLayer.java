/*******************************************************************************
 * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/
package ch.ethz.inf.vs.californium.layers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.ethz.inf.vs.californium.coap.Message;
import ch.ethz.inf.vs.californium.coap.ObservingManager;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
/**
 * This class matches the request/response pairs using the token option. It must
 * be below the {@link TransferLayer}, which requires set buddies for each
 * message ({@link Response#getRequest()} and {@link Request#getResponse()}).
 * 
 * @author Matthias Kovatsch
 */
public class MatchingLayer extends UpperLayer {

// Members /////////////////////////////////////////////////////////////////////
	
	private Map<String, RequestResponsePair> pairs = new ConcurrentHashMap<String, RequestResponsePair>();
	
// Nested Classes //////////////////////////////////////////////////////////////
	
	/*
	 * Entity class to keep state of transfers
	 */
	private static class RequestResponsePair {
		public String key;
		public Request request;
	}
	
	// Constructors ////////////////////////////////////////////////////////////
	
	public MatchingLayer() {

	}

	// I/O implementation //////////////////////////////////////////////////////
	
	@Override
	protected void doSendMessage(Message msg) throws IOException { 
		
		if (msg instanceof Request && !ObservingManager.getInstance().hasSubscription(msg.sequenceKey())) {
			
			addOpenRequest((Request) msg);
		}
		
		sendMessageOverLowerLayer(msg);
	}	
	
	@Override
	protected void doReceiveMessage(Message msg) {
		
		if (msg instanceof Response) {

			Response response = (Response) msg;
			
			RequestResponsePair pair = getOpenRequest(msg.sequenceKey());

			// check for missing token
			if (pair == null && response.getToken().length==0) {
				
				LOG.info(String.format("Remote endpoint failed to echo token: %s", msg.key()));
				
				// TODO try to recover from peerAddress?
				
				if (!ObservingManager.getInstance().hasSubscription(msg.sequenceKey())) {
					// also covers CoAP ping
					msg.reject();
					return;
				}
			}
			
			if (pair != null) {

				// attach request and response to each other
				pair.request.setResponse(response);
				response.setRequest(pair.request);

				LOG.finer(String.format("Matched open request: %s", response.sequenceKey()));
				
				//TODO Cancel possible open transaction (note: waiting for stack redesign)
				removeOpenRequest(response.sequenceKey());
				
			} else if (ObservingManager.getInstance().hasSubscription(msg.sequenceKey())) {
				
				Request observeRequest = ObservingManager.getInstance().getSubscriptionRequest(msg.sequenceKey());

				// attach request and response to each other
				observeRequest.setResponse(response);
				response.setRequest(observeRequest);
			} else {
			
				
				if (response.isConfirmable() || response.getFirstOption(OptionNumberRegistry.OBSERVE)!=null) {

					LOG.info(String.format("Rejecting unexpected response: %s", response.sequenceKey()));
					response.reject();
				} else {
					LOG.info(String.format("Dropping unexpected response: %s", response.sequenceKey()));
				}
				
				return;
			}
			
			if (msg.isConfirmable()) {
				msg.accept();
			}
			
		}
		
		deliverMessage(msg);
	}
	
	private void addOpenRequest(Request request) {
		
		if (request.getFirstOption(OptionNumberRegistry.OBSERVE)!=null) {
			ObservingManager.getInstance().addSubscription(request);
		} else {
			
			if (ObservingManager.getInstance().hasSubscription(request.sequenceKey())) {
				ObservingManager.getInstance().cancelSubscription(request.sequenceKey());
			}
		
			// create new Transaction
			RequestResponsePair exchange = new RequestResponsePair();
			exchange.key = request.sequenceKey();
			exchange.request = request;
			
			LOG.finer(String.format("Storing open request: %s", exchange.key));
			
			// associate token with Transaction
			pairs.put(exchange.key, exchange);
		}
	}
	
	private RequestResponsePair getOpenRequest(String key) {
		return pairs.get(key);
	}
	
	private void removeOpenRequest(String key) {
		
		RequestResponsePair exchange = pairs.remove(key);

		LOG.finer(String.format("Cleared open request: %s", exchange.key));
	}
	
	public String getStats() {
		StringBuilder stats = new StringBuilder();
		
		stats.append("Open requests: ");
		stats.append(pairs.size());
		stats.append('\n');
		stats.append("Messages sent:     ");
		stats.append(numMessagesSent);
		stats.append('\n');
		stats.append("Messages received: ");
		stats.append(numMessagesReceived);
		
		return stats.toString();
	}
}
