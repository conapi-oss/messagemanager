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
package nl.queuemanager.jms.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * This wrapper for a org.w3c.dom.Document serves as a compatibility wrapper
 * between Xerces-J 2.1.0 and the Java 6 implementation of Document. It 
 * implements methods like getXmlStandAlone() and getXmlVersion().
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class Xerces210DocumentWrapper implements Document {

	protected final Document delegate;

	/**
	 * Check if the document needs to be wrapped and wrap it if it does.
	 * 
	 * @param doc
	 * @return The wrapped document or the original document.
	 */
	public static Document wrap(Document doc) {
		if(needToWrap(doc)) {
			System.out.println("Using Xerces-J 2.1.0 workaround");
			return new Xerces210DocumentWrapper(doc);
		}
		
		// No wrapping required, return original document
		return doc;
	}

	/**
	 * Check if the method getXmlStandalone exists on the Document implementation. If
	 * not, we need to wrap the document in the wrapper. If it exists, the wrapper is
	 * not required.
	 * 
	 * @param doc
	 * @return
	 */
	public static boolean needToWrap(Document doc) {
		try {
			Method getXmlStandalone = 
				doc.getClass().getMethod("getXmlStandalone", (Class[])null);
			
			return getXmlStandalone == null 
				|| Modifier.isAbstract(getXmlStandalone.getModifiers());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private Xerces210DocumentWrapper(Document delegate) {
		this.delegate = delegate;
	}
	
	public Node adoptNode(Node node) throws DOMException {
		return delegate.adoptNode(node);
	}

	public Node appendChild(Node node) throws DOMException {
		return delegate.appendChild(node);
	}

	public Node cloneNode(boolean flag) {
		return delegate.cloneNode(flag);
	}

	public short compareDocumentPosition(Node node) throws DOMException {
		return delegate.compareDocumentPosition(node);
	}

	public Attr createAttribute(String s) throws DOMException {
		return delegate.createAttribute(s);
	}

	public Attr createAttributeNS(String s, String s1) throws DOMException {
		return delegate.createAttributeNS(s, s1);
	}

	public CDATASection createCDATASection(String s) throws DOMException {
		return delegate.createCDATASection(s);
	}

	public Comment createComment(String s) {
		return delegate.createComment(s);
	}

	public DocumentFragment createDocumentFragment() {
		return delegate.createDocumentFragment();
	}

	public Element createElement(String s) throws DOMException {
		return delegate.createElement(s);
	}

	public Element createElementNS(String s, String s1) throws DOMException {
		return delegate.createElementNS(s, s1);
	}

	public EntityReference createEntityReference(String s) throws DOMException {
		return delegate.createEntityReference(s);
	}

	public ProcessingInstruction createProcessingInstruction(String s, String s1)
			throws DOMException {
		return delegate.createProcessingInstruction(s, s1);
	}

	public Text createTextNode(String s) {
		return delegate.createTextNode(s);
	}

	public NamedNodeMap getAttributes() {
		return delegate.getAttributes();
	}

	public String getBaseURI() {
		return delegate.getBaseURI();
	}

	public NodeList getChildNodes() {
		return delegate.getChildNodes();
	}

	public DocumentType getDoctype() {
		return delegate.getDoctype();
	}

	public Element getDocumentElement() {
		return delegate.getDocumentElement();
	}

	public String getDocumentURI() {
		return delegate.getDocumentURI();
	}

	public DOMConfiguration getDomConfig() {
		return delegate.getDomConfig();
	}

	public Element getElementById(String s) {
		return delegate.getElementById(s);
	}

	public NodeList getElementsByTagName(String s) {
		return delegate.getElementsByTagName(s);
	}

	public NodeList getElementsByTagNameNS(String s, String s1) {
		return delegate.getElementsByTagNameNS(s, s1);
	}

	public Object getFeature(String s, String s1) {
		return delegate.getFeature(s, s1);
	}

	public Node getFirstChild() {
		return delegate.getFirstChild();
	}

	public DOMImplementation getImplementation() {
		return delegate.getImplementation();
	}

	public String getInputEncoding() {
		return delegate.getInputEncoding();
	}

	public Node getLastChild() {
		return delegate.getLastChild();
	}

	public String getLocalName() {
		return delegate.getLocalName();
	}

	public String getNamespaceURI() {
		return delegate.getNamespaceURI();
	}

	public Node getNextSibling() {
		return delegate.getNextSibling();
	}

	public String getNodeName() {
		return delegate.getNodeName();
	}

	public short getNodeType() {
		return delegate.getNodeType();
	}

	public String getNodeValue() throws DOMException {
		return delegate.getNodeValue();
	}

	public Document getOwnerDocument() {
		return delegate.getOwnerDocument();
	}

	public Node getParentNode() {
		return delegate.getParentNode();
	}

	public String getPrefix() {
		return delegate.getPrefix();
	}

	public Node getPreviousSibling() {
		return delegate.getPreviousSibling();
	}

	public boolean getStrictErrorChecking() {
		return delegate.getStrictErrorChecking();
	}

	public String getTextContent() throws DOMException {
		return delegate.getTextContent();
	}

	public Object getUserData(String s) {
		return delegate.getUserData(s);
	}

	public String getXmlEncoding() {
		return "UTF-8";
	}

	public boolean getXmlStandalone() {
		return true;
	}

	public String getXmlVersion() {
		return "1.0";
	}

	public boolean hasAttributes() {
		return delegate.hasAttributes();
	}

	public boolean hasChildNodes() {
		return delegate.hasChildNodes();
	}

	public Node importNode(Node node, boolean flag) throws DOMException {
		return delegate.importNode(node, flag);
	}

	public Node insertBefore(Node node, Node node1) throws DOMException {
		return delegate.insertBefore(node, node1);
	}

	public boolean isDefaultNamespace(String s) {
		return delegate.isDefaultNamespace(s);
	}

	public boolean isEqualNode(Node node) {
		return delegate.isEqualNode(node);
	}

	public boolean isSameNode(Node node) {
		return delegate.isSameNode(node);
	}

	public boolean isSupported(String s, String s1) {
		return delegate.isSupported(s, s1);
	}

	public String lookupNamespaceURI(String s) {
		return delegate.lookupNamespaceURI(s);
	}

	public String lookupPrefix(String s) {
		return delegate.lookupPrefix(s);
	}

	public void normalize() {
		delegate.normalize();
	}

	public void normalizeDocument() {
		delegate.normalizeDocument();
	}

	public Node removeChild(Node node) throws DOMException {
		return delegate.removeChild(node);
	}

	public Node renameNode(Node node, String s, String s1) throws DOMException {
		return delegate.renameNode(node, s, s1);
	}

	public Node replaceChild(Node node, Node node1) throws DOMException {
		return delegate.replaceChild(node, node1);
	}

	public void setDocumentURI(String s) {
		delegate.setDocumentURI(s);
	}

	public void setNodeValue(String s) throws DOMException {
		delegate.setNodeValue(s);
	}

	public void setPrefix(String s) throws DOMException {
		delegate.setPrefix(s);
	}

	public void setStrictErrorChecking(boolean flag) {
		delegate.setStrictErrorChecking(flag);
	}

	public void setTextContent(String s) throws DOMException {
		delegate.setTextContent(s);
	}

	public Object setUserData(String s, Object obj,
			UserDataHandler userdatahandler) {
		return delegate.setUserData(s, obj, userdatahandler);
	}

	public void setXmlStandalone(boolean flag) throws DOMException {
		return;
	}

	public void setXmlVersion(String s) throws DOMException {
		return;
	}
	
}
