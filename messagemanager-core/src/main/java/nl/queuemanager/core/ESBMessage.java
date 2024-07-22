/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.core;

import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.MessageType;
import nl.queuemanager.jms.impl.MessageFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Base64;
import java.util.Enumeration;

/**
 * This class reads and writes .esbmsg files. 
 * 
 * These files have the following structure:<br>
 * <pre>
 *  &lt;" + getQualifiedMessageElementName() + " type="Multipart-Message" xmlns:sonic_esbmsg="http://sonicsw.com/tools/esbmsg/namespace">
 *		&lt;header name="JMSCorrelationID" value="Header-Value">&lt;/header>
 *		&lt;header name="JMSReplyTo" value="dev.MessageListener">&lt;/header>
 *		&lt;header name="JMSType" value='Multipart Message'>&lt;/header>
 *		&lt;property name="new-name0" value="new-value">&lt;/property>
 *		&lt;property name="new-name1" value="new-value">&lt;/property>
 *		&lt;property name="new-name2" value="new-value">&lt;/property>
 *
 *		&lt;part content-type="text/xml" content-id="new-content-id0" file-ref="" use-file-ref="false">&lt;/part>
 *		&lt;part content-type="text/xml" content-id="new-content-id1" file-ref="" use-file-ref="false">&lt;/part>
 *		&lt;part content-type="text/xml" content-id="new-content-id2" file-ref="" use-file-ref="false">&lt;/part>
 *
 *		or
 *
 *      &lt;body content-type="text/plain" content-id="body-part" file-ref="sonicfs:///workspace/Test/multipart.esbmsg" use-file-ref="true">&lt;/body>
 *	&lt;/" + getQualifiedMessageElementName() + ">
 * </pre>
 * *
 *
 */
public final class ESBMessage extends BaseMessage {

	private static String MSG_PREFIX = "sonic_esbmsg";
	private static String MSG_NAMESPACE = "http://sonicsw.com/tools/esbmsg/namespace";
	private static String MSG_ROOT_ELEMENT= "esbmsg";
	private static String FILE_EXTENSION= ".esbmsg";

	{
		initialize(MSG_PREFIX, MSG_NAMESPACE);
	}

	public static String getFileExtension() {
		return FILE_EXTENSION;
	}

	// in order to keep the static approach we need these methods implemented in the subclasses
	protected String getMessageNamespace() {
		return MSG_NAMESPACE;
	}
	protected String getQualifiedMessageElementName()	 {
		return MSG_PREFIX + ":" + MSG_ROOT_ELEMENT;
	}

}