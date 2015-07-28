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

package ch.ethz.inf.vs.californium.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.dtls.CipherSuite;

/**
 * This class implements Californium's property registry.
 * 
 * It is used to manage CoAP- and Californium-specific constants in a central
 * place. The properties are initialized in the init() section and can be
 * overridden by a user-defined .properties file. If the file does not exist
 * upon initialization, it will be created so that a valid configuration always
 * exists.
 * 
 * @author Dominique Im Obersteg, Daniel Pauli, and Matthias Kovatsch
 */
public class Properties extends java.util.Properties {

	private static final Logger LOG = Logger.getLogger(Properties.class.getName());

	/**
	 * auto-generated to eliminate warning
	 */
	private static final long serialVersionUID = -8883688751651970877L;

	/** The header for Californium property files. */
	private static final String HEADER = "Californium CoAP Properties file";

	/** The name of the default properties file. */
	private static final String DEFAULT_FILENAME = "Californium.properties";

	// default properties used by the library
	public static final Properties std = new Properties(DEFAULT_FILENAME);
	
	// Constructors ////////////////////////////////////////////////////////////
	
	public Properties(String fileName) {
		init();
		initUserDefined(fileName);
	}
	
	public Double getDbl(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				LOG.severe(String.format("Invalid double property: %s=%s", key, value));
			}
		} else {
			LOG.severe(String.format("Undefined double property: %s", key));
		}
		return 0.0;
	}

	public int getInt(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				LOG.severe(String.format("Invalid integer property: %s=%s", key, value));
			}
		} else {
			LOG.severe(String.format("Undefined integer property: %s", key));
		}
		return 0;
	}

	public String getStr(String key) {
		String value = getProperty(key);
		if (value == null) {
			LOG.severe(String.format("Undefined string property: %s", key));
		}
		return value;
	}

	public boolean getBool(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				LOG.severe(String.format("Invalid boolean property: %s=%s", key, value));
			}
		} else {
			LOG.severe(String.format("Undefined boolean property: %s", key));
		}
		return false;
	}
	
	public void load(String fileName) throws IOException {
		InputStream in = new FileInputStream(fileName);
		load(in);
	}

	public void set(String key, double value) {
		setProperty(key, String.valueOf(value));
	}

	public void set(String key, int value) {
		setProperty(key, String.valueOf(value));
	}

	public void set(String key, String value) {
		setProperty(key, value);
	}
	
	public void set(String key, boolean value) {
		setProperty(key, String.valueOf(value));
	}

	public void store(String fileName) throws IOException {
		OutputStream out = new FileOutputStream(fileName);
		store(out, HEADER);
	}

	private void init() {

		/* CoAP Protocol constants */

		// default CoAP port as defined in draft-ietf-core-coap-05, section 7.1:
		// MUST be supported by a server for resource discovery and
		// SHOULD be supported for providing access to other resources.
		set("DEFAULT_PORT", 5683);

		// constants to calculate initial timeout for confirmable messages,
		// used by the exponential backoff mechanism
		set("RESPONSE_TIMEOUT", 2000); // [milliseconds]
		set("RESPONSE_RANDOM_FACTOR", 1.5);

		// maximal number of retransmissions before the attempt
		// to transmit a message is canceled
		set("MAX_RETRANSMIT", 4);

		/* Implementation-specific */

		// buffer size for incoming datagrams, in bytes
		// TODO find best value
		set("RX_BUFFER_SIZE", 4 * 1024); // [bytes]

		// capacity for caches used for duplicate detection and retransmissions
		set("MESSAGE_CACHE_SIZE", 32); // [messages]

		// time limit for transactions to complete,
		// used to avoid infinite waits for replies to non-confirmables
		// and separate responses
		// FIXME MAX_LATENCY in the draft?
		set("DEFAULT_OVERALL_TIMEOUT", 100000); // [milliseconds]

		// the default block size for block-wise transfers
		// must be power of two between 16 and 1024
		set("DEFAULT_BLOCK_SIZE", 512); // [bytes]
		
		// proxy http port
		set("HTTP_PORT", 8080);

		// timeout for the tcp socket of the http server
		// => coherent with the DEFAULT_OVERALL_TIMEOUT

		// buffer size for the http server
		set("HTTP_SERVER_SOCKET_BUFFER_SIZE", 8 * 1024);

		// number of threads that are handling the resource dispatching
		set("THREAD_POOL_SIZE", 10);

		// number of millis to maintain open the http client connection
		set("HTTP_CLIENT_KEEP_ALIVE", 5000);

		// number of seconds before a cached request becomes available for the
		// eviction
		// 60 * 60 * 24 => 1 day
		set("CACHE_RESPONSE_MAX_AGE", 60 * 60 * 24);

		// number of entries contained in the cache
		set("CACHE_SIZE", 10000);

		// the number of notifications until a CON notification will be used
		set("OBSERVING_REFRESH_INTERVAL", 10);

		// the domain for Resource Directory entries when the endpoint does not specify one
		set("RD_DEFAULT_DOMAIN", "local");
		
		// the time until the Resource Directory validates endpoints that did not provide a lifetime
		set("RD_DEFAULT_LIFETIME", 24 * 60 * 60);

		// the time until the Resource Directory validates endpoints that did not provide a lifetime
		set("RD_VALIDATION_TIMEOUT", 240);

		/* DTLS constants */
		
		// whether DTLS should be enabled 
		set("ENABLE_DTLS", false);

		// whether the certificate message should only contain the peer's public
		// key or the full X.509 certificate
		set("USE_RAW_PUBLIC_KEY", true);

		// whether the server requires mutual authentication
		set("CLIENT_AUTHENTICATION", false);

		// the location of the key store (contains private key and corresponding
		// certificate chain)
		set("KEY_STORE_LOCATION", "path/to/keyStore.jks");

		// the location of the trust store (contains all trusted certificate
		// authorities)
		set("TRUST_STORE_LOCATION", "path/to/trustStore.jks");

		// the preferred cipher suite
		set("PREFERRED_CIPHER_SUITE", CipherSuite.TLS_PSK_WITH_AES_128_CCM_8.toString());

		// the maximum fragment size before DTLS fragmentation must be applied
		set("MAX_FRAGMENT_LENGTH", 200); // [bytes]

		// the initial timer value for retransmission; rfc6347, section: 4.2.4.1
		set("RETRANSMISSION_TIMEOUT", 1000); // [milliseconds]

		// the identity hint when using the pre-shared key mode
		set("PSK_IDENTITY", "PSK_Identity");
	}

	private void initUserDefined(String fileName) {
		try {
			load(fileName);
		} catch (IOException e) {
			// file does not exist:
			// write default properties
			try {
				store(fileName);
			} catch (IOException e1) {
				LOG.warning(String.format("Failed to create configuration file: %s", e1.getMessage()));
			}
		}
	}

}
