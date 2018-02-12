package nl.queuemanager.solace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.extern.java.Log;
import nl.queuemanager.core.DebugProperty;

@Log
class HttpSEMPConnection implements SempConnection {
	
	@Getter private final URI uri;
	@Getter private final SmfConnectionDescriptor connectionDescriptor;
	
	public HttpSEMPConnection(SempConnectionDescriptor descriptor) throws SempException {
		this.connectionDescriptor = descriptor;
		// FIXME This doesn't use the TLS properties and such.
		this.uri = descriptor.createHttpUri();
	}
	
	public static String getApplianceName(URI uri) throws SempException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final AtomicReference<String> result = new AtomicReference<>();
		performSEMPRequest(uri, SempRequests.showHostname(), new SempResponseCallback() {
			@Override
			public void handle(Document doc) throws Exception {
				result.set(xpath.evaluate("/rpc-reply/rpc/show/hostname/hostname", doc));
			}
		});
		return result.get();
	}
	
	public String getApplianceName() throws SempException {
		return getApplianceName(uri);
	}
	
	public static String getMessagingIP(URI uri) throws SempException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final List<String> ipAddresses = new ArrayList<String>();
		
		final SempResponseCallback callback = new SempResponseCallback() {
 			@Override
			public void handle(Document doc) throws Exception {
				NodeList interfaces = (NodeList)xpath.evaluate("/rpc-reply/rpc/show/ip/vrf/vrf-element/interfaces/intf-element", doc, XPathConstants.NODESET);
				for(int i=0; i<interfaces.getLength(); i++) {
					Element intf = (Element)interfaces.item(i);
					String ip = xpath.evaluate("ip-addr", intf);
					
					ipAddresses.add(ip);
				}
			}
		};
		
		// Get msg-backbone IP address(es)
		performSEMPRequest(uri, SempRequests.showIpVrfMsgBackbone(), callback);
		
		// If there are no IP addresses, we must be trying to connect to a VMR. Try the management VRF
		if(ipAddresses.size() == 0) {
			performSEMPRequest(uri, SempRequests.showIpVrfManagement(), callback);
		}

		// TODO Select most appropriate one (primary vrrp ip or static if not available)
		String ip = ipAddresses.get(0);
		if(ip.contains("/")) {
			ip = ip.substring(0, ip.indexOf('/'));
		}
		return ip;
	}
	public String getMessagingIP() throws SempException {
		return getMessagingIP(uri);
	}
	
	public static int getSmfPort(URI uri) throws SempException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final AtomicReference<Integer> result = new AtomicReference<>();

		performSEMPRequest(uri, SempRequests.showService(), new SempResponseCallback() {
			@Override
			public void handle(Document doc) throws Exception {
				result.set(((Number)xpath.evaluate("/rpc-reply/rpc/show/service/services/service[name='SMF']/listen-port", doc, XPathConstants.NUMBER)).intValue());
			}
		});
		
		return result.get();
	}
	
	public int getSmfPort() throws SempException {
		return getSmfPort(uri);
	}
	
	@Override
	public void showMessageVPN(String vpnName, SempResponseCallback responseHandler) throws SempException {
		performSEMPRequest(uri, SempRequests.showMessageVPN(vpnName), responseHandler);
	}
	
	@Override
	public void showMessageSpoolByVPN(String vpnName, SempResponseCallback responseHandler) throws SempException {
		performSEMPRequest(uri, SempRequests.showMessageSpoolByVpn(vpnName), responseHandler);
	}
	
	@Override
	public void performShowRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException {
		performSEMPRequest(uri, request, sempResponseHandler);
	}

	@Override
	public void performAdminRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException {
		performSEMPRequest(uri, request, sempResponseHandler);
	}

	protected static void performSEMPRequest(URI uri, byte[] request, SempResponseCallback handler) throws SempException {		
		byte[] requestBytes = request;
		boolean haveMoreCookie;
		do try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			XPath xp = XPathFactory.newInstance().newXPath();
			
			haveMoreCookie = false;
			
			// Send the request
			
			log.finer("Sending request:\n" + new String(requestBytes));
			HttpURLConnection conn = sendRequest(uri.toURL(), requestBytes);
			
			// Get the response as a byte array
			byte[] responseBytes = readFully(conn);
			log.finer("Got response:\n" + new String(responseBytes));
			
			// Interpret the response
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				try {
					Document doc = db.parse(new InputSource(new ByteArrayInputStream(responseBytes)));
	
					// Check the execute-result to see if it's "ok"
					String executeResult = xp.evaluate("/rpc-reply/execute-result/@code", doc);
					boolean success = "ok".equals(executeResult);
	
					if(!success) {
						// Find out if this is a permission problem
						String permissionError = xp.evaluate("/rpc-reply/permission-error", doc);
						if(!Strings.isNullOrEmpty(permissionError)) {
							throw new SempException(403, "Forbidden", responseBytes, permissionError);
						} else {
							// Some other kind of error
							throw new SempException(conn.getResponseCode(), conn.getResponseMessage(), responseBytes, "SEMP request failed");
						}
					}
						
					// Check if there is a more-cookie and set up for the next request, if any
					Element moreCookie = (Element) xp.evaluate("/rpc-reply/more-cookie/rpc", doc, XPathConstants.NODE);
					if(moreCookie != null) {
						try {
							requestBytes = XmlUtils.serialize(moreCookie);
						} catch (TransformerException e) {
							throw new SempException(conn.getResponseCode(), conn.getResponseMessage(), responseBytes, "Unable to serialize more-cookie from request", e);
						}
						haveMoreCookie = true;
					}
					
					// Handle the response
					try {
						handler.handle(doc);
					} catch (Exception e) {
						throw new SempException("SEMPResponseCallback threw Exception", e);
					}
				} catch (SAXException | XPathExpressionException e) {
					throw new SempException(conn.getResponseCode(), conn.getResponseMessage(), responseBytes, "XML handling exception", e);
				}
			} else {
				throw new SempException(conn.getResponseCode(), conn.getResponseMessage(), responseBytes);
			}
		} catch (IOException | ParserConfigurationException e) {
			throw new SempException("Exception during SEMP request", e);
		} while(haveMoreCookie);
	}
	
	static HttpURLConnection sendRequest(URL url, byte[] requestBytes) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(5000); // TODO Make timeout configurable
		String userInfo = url.getUserInfo();
		if(userInfo != null && userInfo.trim().length() > 0) {
			String base64 = javax.xml.bind.DatatypeConverter.printBase64Binary(userInfo.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + base64);
		}
		conn.setRequestProperty("Content-Length", Integer.toString(requestBytes.length));
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.getOutputStream().write(requestBytes);
		return conn;
	}
	
	static byte[] readFully(URLConnection conn) throws IOException {
		int length = Integer.parseInt(conn.getHeaderField("Content-Length"));
		if(length == 0) {
			return new byte[0];
		}
		
		try(InputStream in = conn.getInputStream()) {
			byte[] response = new byte[length];
			int off=0;
			while(off < length) {
				int n = in.read(response, off, length-off);
				if(n == -1) {
					log.severe("Stream at end of file?!?!?!");
					System.exit(1);
				}
				off += n;
			}
			return response;
		}
	}

	@Override
	public SmfConnectionDescriptor getSmfConnectionDescriptor() {
		return connectionDescriptor;
	}

}