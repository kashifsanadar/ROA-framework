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
package ch.ethz.inf.vs.californium.dtls;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.dtls.AlertMessage.AlertDescription;
import ch.ethz.inf.vs.californium.dtls.AlertMessage.AlertLevel;
import ch.ethz.inf.vs.californium.util.DatagramReader;
import ch.ethz.inf.vs.californium.util.DatagramWriter;

/**
 * Represents a structure to hold several {@link HelloExtension}.
 * 
 * @author Stefan Jucker
 * 
 */
public class HelloExtensions {
	// Logging ////////////////////////////////////////////////////////

	private static final Logger LOG = Logger.getLogger(HelloExtensions.class.getName());

	// DTLS-specific constants ////////////////////////////////////////

	private static final int LENGTH_BITS = 16;

	private static final int EXTENSION_LENGTH_BITS = 16;

	private static final int TYPE_BITS = 16;

	// Members ////////////////////////////////////////////////////////

	/** The list of extensions. */
	private List<HelloExtension> extensions;

	// Constructors ///////////////////////////////////////////////////

	public HelloExtensions() {
		this.extensions = new ArrayList<HelloExtension>();
	}

	public HelloExtensions(List<HelloExtension> extensions) {
		this.extensions = extensions;
	}

	// Methods ////////////////////////////////////////////////////////

	/**
	 * 
	 * @return the length of the whole extension fragment.
	 */
	public int getLength() {
		int length = 0;
		for (HelloExtension extension : extensions) {
			length += extension.getLength();
		}

		return length;
	}

	public void addExtension(HelloExtension extension) {
		this.extensions.add(extension);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\tExtensions Length: " + getLength() + "\n");
		for (HelloExtension ext : extensions) {
			sb.append(ext.toString());
		}
		return sb.toString();
	}

	// Serialization //////////////////////////////////////////////////

	public byte[] toByteArray() {
		DatagramWriter writer = new DatagramWriter();

		writer.write(getLength(), LENGTH_BITS);
		for (HelloExtension extension : extensions) {
			writer.writeBytes(extension.toByteArray());
		}

		return writer.toByteArray();
	}

	public static HelloExtensions fromByteArray(byte[] byteArray) throws HandshakeException {
		DatagramReader reader = new DatagramReader(byteArray);
		List<HelloExtension> extensions = new ArrayList<HelloExtension>();

		int length = reader.read(LENGTH_BITS);

		while (length > 0) {

			ExtensionType type = ExtensionType.getExtensionTypeById(reader.read(TYPE_BITS));
			int extensionLength = reader.read(EXTENSION_LENGTH_BITS);
			
			if (type != null) {
				HelloExtension helloExtension = HelloExtension.fromByteArray(reader.readBytes(extensionLength), type);
				extensions.add(helloExtension);
			}

			// the extensions length + 2 bytes for type field and 2 bytes for
			// length field
			length -= extensionLength + 4;
		}

		return new HelloExtensions(extensions);
	}

	// Extension type Enum ////////////////////////////////////////////

	/**
	 * The possible extension types (defined in multiple documents). See <a
	 * href=
	 * "http://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xml"
	 * >IANA</a> for a summary.
	 * 
	 * @author Stefan Jucker
	 * 
	 */
	public enum ExtensionType {
		/** See <a href="http://www.ietf.org/rfc/rfc3546">RFC 3546</a> */
		SERVER_NAME(0, "server_name"),
		MAX_FRAGMENT_LENGTH(1, "max_fragment_length"),
		CLIENT_CERTIFICATE_URL(2, "client_certificate_url"),
		TRUSTED_CA_KEYS(3, "trusted_ca_keys"),
		TRUNCATED_HMAC(4, "truncated_hmac"),
		STATUS_REQUEST(5, "status_request"),

		/** See <a href="http://tools.ietf.org/html/rfc4681">RFC 4681</a> */
		USER_MAPPING(6, "user_mapping"),
		
		/** See <a href="http://www.iana.org/go/rfc5878">RFC 5878</a> */
		CLIENT_AUTHZ(7, "client_authz"),
		SERVER_AUTHZ(8, "server_authz"),

		/**
		 * See <a href=
		 * "http://tools.ietf.org/html/draft-ietf-tls-oob-pubkey-03#section-3.1"
		 * >TLS Out-of-Band Public Key Validation</a>
		 */
		CERT_TYPE(9, "cert_type"),

		/**
		 * See <a href="http://tools.ietf.org/html/rfc4492#section-5.1">RFC
		 * 4492</a>
		 */
		ELLIPTIC_CURVES(10, "elliptic_curves"),
		EC_POINT_FORMATS(11, "ec_point_formats"),
		
		/** See <a href="http://www.iana.org/go/rfc5054">RFC 5054</a> */
		SRP(12, "srp"),
		
		/** See <a href="http://www.iana.org/go/rfc5246">RFC 5246</a> */
		SIGNATURE_ALGORITHMS(13, "signature_algorithms"),
		
		/** See <a href="http://www.iana.org/go/rfc5764">RFC 5764</a> */
		USE_SRTP(14, "use_srtp"),
		
		/** See <a href="http://www.iana.org/go/rfc6520">RFC 6520</a> */
		HEARTBEAT(15, "heartbeat"),
		
		/** See <a href="http://www.iana.org/go/draft-friedl-tls-applayerprotoneg">draft-friedl-tls-applayerprotoneg</a> */
		APPLICATION_LAYER_PROTOCOL_NEGOTIATION(16, "application_layer_protocol_negotiation"),

		/** See <a href="http://www.iana.org/go/draft-ietf-tls-multiple-cert-status-extension-08">draft-ietf-tls-multiple-cert-status-extension-08</a> */
		STATUS_REQUEST_V2(17, "status_request_v2"),
		
		/** See <a href="http://www.iana.org/go/draft-laurie-pki-sunlight-12">draft-laurie-pki-sunlight-12</a> */
		SIGNED_CERTIFICATE_TIMESTAMP(18, "signed_certificate_timestamp"),
		
		/** TODO: Values not assigned yet */
		CLIENT_CERT_TYPE(122, "client_certificate_type"),
		SERVER_CERT_TYPE(123, "server_certificate_type");
		

		private int id;

		private String name;

		ExtensionType(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public static ExtensionType getExtensionTypeById(int id) throws HandshakeException {
			switch (id) {
			case 0:
				return ExtensionType.SERVER_NAME;
			case 1:
				return ExtensionType.MAX_FRAGMENT_LENGTH;
			case 2:
				return ExtensionType.CLIENT_CERTIFICATE_URL;
			case 3:
				return ExtensionType.TRUSTED_CA_KEYS;
			case 4:
				return ExtensionType.TRUNCATED_HMAC;
			case 5:
				return ExtensionType.STATUS_REQUEST;
			case 6:
				return ExtensionType.USER_MAPPING;
			case 7:
				return ExtensionType.CLIENT_AUTHZ;
			case 8:
				return ExtensionType.SERVER_AUTHZ;
			case 9:
				return ExtensionType.CERT_TYPE;
			case 10:
				return ExtensionType.ELLIPTIC_CURVES;
			case 11:
				return ExtensionType.EC_POINT_FORMATS;
			case 12:
				return ExtensionType.SRP;
			case 13:
				return ExtensionType.SIGNATURE_ALGORITHMS;
			case 14:
				return ExtensionType.USE_SRTP;
			case 15:
				return ExtensionType.HEARTBEAT;
			case 16:
				return ExtensionType.APPLICATION_LAYER_PROTOCOL_NEGOTIATION;
			case 17:
				return ExtensionType.STATUS_REQUEST_V2;
			case 18:
				return ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP;
			case 122:
				// TODO value TBD
				return ExtensionType.CLIENT_CERT_TYPE;
			case 123:
				// TODO value TBD
				return ExtensionType.SERVER_CERT_TYPE;

			default:
				LOG.severe("Unknown extension type code: " + id);
				AlertMessage alert = new AlertMessage(AlertLevel.FATAL, AlertDescription.UNSUPPORTED_EXTENSION);
				throw new HandshakeException("Unknown extension type code received: " + id, alert);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		public int getId() {
			return id;
		}
	}

	public List<HelloExtension> getExtensions() {
		return extensions;
	}
}
