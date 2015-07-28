package ifi.coap;

public class CoAPTest {

	public static void main(String args[]) throws Exception {
		
		CoapClient coapClient = new CoapClient();

		int i = 0;
		while (i < 10) {
		double start = System.currentTimeMillis();
		String linkFormat = coapClient.sendCoAPRequest(
				"coap://[aaaa::212:7400:1360:c66b]:5683/sensors/temperature",
				"GET");
		System.out.println("Response time: " + (System.currentTimeMillis() - start));

		i++;
		
		}
		
		
	}

}
