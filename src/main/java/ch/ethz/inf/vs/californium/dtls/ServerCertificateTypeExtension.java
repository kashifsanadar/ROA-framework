package ch.ethz.inf.vs.californium.dtls;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.californium.dtls.HelloExtensions.ExtensionType;
import ch.ethz.inf.vs.californium.util.DatagramReader;

public class ServerCertificateTypeExtension extends CertificateTypeExtension {

	// Constructors ///////////////////////////////////////////////////
	
	/**
	 * Constructs an empty certificate type extension. If it is client-sided
	 * there is a list of supported certificate type (ordered by preference);
	 * server-side only 1 certificate type is chosen.
	 * 
	 * @param isClient
	 *            whether this instance is considered the client.
	 */
	public ServerCertificateTypeExtension(boolean isClient) {
		super(ExtensionType.SERVER_CERT_TYPE, isClient);
	}
	
	/**
	 * Constructs a certificate type extension with a list of supported
	 * certificate types. The server only chooses 1 certificate type.
	 * 
	 * @param certificateTypes
	 *            the list of supported certificate types.
	 * @param isClient
	 *            whether this instance is considered the client.
	 */
	public ServerCertificateTypeExtension(boolean isClient, List<CertificateType> certificateTypes) {
		super(ExtensionType.SERVER_CERT_TYPE, isClient, certificateTypes);
	}

	// Methods ////////////////////////////////////////////////////////

	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());

		for (CertificateType type : certificateTypes) {
			sb.append("\t\t\t\tServer certificate type: " + type.toString() + "\n");
		}

		return sb.toString();
	};
	
	public static HelloExtension fromByteArray(byte[] byteArray) {
		DatagramReader reader = new DatagramReader(byteArray);
		
		List<CertificateType> certificateTypes = new ArrayList<CertificateType>();
		
		// the client's extension needs at least 2 bytes, while the server's is exactly 1 byte long
		boolean isClientExtension = true;
		if (byteArray.length > 1) {
			int length = reader.read(LIST_FIELD_LENGTH_BITS);
			for (int i = 0; i < length; i++) {
				certificateTypes.add(CertificateType.getTypeFromCode(reader.read(EXTENSION_TYPE_BITS)));
			}
		} else {
			certificateTypes.add(CertificateType.getTypeFromCode(reader.read(EXTENSION_TYPE_BITS)));
			isClientExtension = false;
		}

		return new ServerCertificateTypeExtension(isClientExtension, certificateTypes);
	}

}
