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
package nl.queuemanager.ui.message;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.queuemanager.core.Xerces210DocumentWrapper;
import nl.queuemanager.core.jms.JMSMultipartMessage;
import nl.queuemanager.core.jms.JMSPart;
import nl.queuemanager.core.jms.JMSXMLMessage;
import nl.queuemanager.core.util.NullEntityResolver;
import nl.queuemanager.ui.MessageListTransferable;
import nl.queuemanager.ui.util.JSearchableTextArea;
import nl.queuemanager.ui.util.TreeNodeInfo;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jhe.hexed.JHexEditor;

public class MessageViewerPanel extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JScrollPane treeScrollPane = null;
	private JTree structureTree = null;
	
	private Message message = null;
	private JScrollPane infoScrollPane = null;
	
	/**
	 * This is the default constructor
	 */
	public MessageViewerPanel() {
		super();
		initialize();
		
		// Select the "JMS Properties" node
		structureTree.getSelectionModel().setSelectionPath(structureTree.getPathForRow(0));
	}
	
	public void setMessage(Message message) {
		this.message = message;
		
		int[] selectedRows = structureTree.getSelectionModel().getSelectionRows(); 
		
		fillTree((DefaultMutableTreeNode)structureTree.getModel().getRoot(), message);
		
		// Try to select the same rows as before
		structureTree.setSelectionRows(selectedRows);
		
		// If the selection is empty, select the "JMS Properties" node
		if(structureTree.getSelectionModel().isSelectionEmpty())
			structureTree.getSelectionModel().setSelectionPath(structureTree.getPathForRow(0));
	}

	private void fillTree(DefaultMutableTreeNode root, Message message) {
		DefaultTreeModel model = (DefaultTreeModel) structureTree.getModel();
		
		root.removeAllChildren();
		JMSHeadersTable messagePropertiesTable = new JMSHeadersTable();
		messagePropertiesTable.setMessage(this.message);
		root.add(
			new DefaultMutableTreeNode(
				new TreeNodeInfo(
					"JMS Headers", 
					messagePropertiesTable)));
		
		MessagePropertiesTable messageHeadersTable = new MessagePropertiesTable();
		messageHeadersTable.setMessage(this.message);
		root.add(
			new DefaultMutableTreeNode(
				new TreeNodeInfo(
					"Properties", 
					messageHeadersTable)));
		
		if(message != null) 
		try {
			if(message instanceof JMSMultipartMessage) {
				JMSMultipartMessage mp = (JMSMultipartMessage)message;
				for(int i=0; i<mp.getPartCount(); i++) {
					JMSPart part = mp.getPart(i);
					DefaultMutableTreeNode partNode = null;
					if(part.getContent() instanceof Message) {
						 partNode = 
							new DefaultMutableTreeNode(
								new TreeNodeInfo(
									"Part " + i + " (" + part.getContentType() + ")", 
									null,
									part));
						fillTree(partNode, (Message)part.getContent());
						root.add(partNode);
					} else {
						LazyUILoader uiLoader = getPartUILoader(part);
						partNode = 
							new DefaultMutableTreeNode(
								new TreeNodeInfo(
									"Part " + i + " (" + part.getContentType() + ")", 
									uiLoader,
									part));
						root.add(partNode);
						
						MessagePartHeadersTable partHeadersTable = new MessagePartHeadersTable();
						partHeadersTable.setMessagePart(part);
						partNode.add(
								new DefaultMutableTreeNode(
										new TreeNodeInfo(
												"Headers", 
												partHeadersTable)));
					}
				}
			} else if(message instanceof JMSXMLMessage) {
				root.add(
						new DefaultMutableTreeNode(
								new TreeNodeInfo(
										"Body (Xml)", 
										new LazyXmlMessageLoader((JMSXMLMessage)message),
										message)));
			} else if(message instanceof TextMessage) {
				root.add(
						new DefaultMutableTreeNode(
							new TreeNodeInfo(
								"Body (Text)", 
								new LazyXmlMessageLoader((TextMessage)message),
								message)));
			} else if(message instanceof BytesMessage) {
				root.add(
					new DefaultMutableTreeNode(
						new TreeNodeInfo(
							"Body (Bytes)", 
							new LazyBytesMessageLoader((BytesMessage)message),
							message)));
			} else {
				root.add(
					new DefaultMutableTreeNode(
						new TreeNodeInfo(
							"Unknown content",
							new LazyStringLoader("Unable to display content of " + message.getClass().getSimpleName()),
							message)));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Refresh the tree
		model.reload();
		
		// Expand all nodes
		for (int i = 0; i < structureTree.getRowCount(); i++) {
			structureTree.expandRow(i);
		}
	}

	private LazyUILoader getPartUILoader(JMSPart part) {
		String contentType = part.getContentType();
		if(contentType.startsWith("text/")
		|| contentType.startsWith("application/x-sonicxq-bpheader")) {
			return new LazyXmlPartLoader(part);
		} else
		if(contentType.equals("application/x-sonicmq-textmessage")
		|| contentType.equals("application/x-sonicmq-xmlmessage")) {
			return new LazyXmlMessagePartLoader(part);
		} else
		if(contentType.equals("application/x-sonicmq-multipartmessage"))
		{
			return new LazyMultipartMessagePartLoader(part);
		} else {
			return new LazyBytesPartLoader(part);
		}
	}

	public void valueChanged(TreeSelectionEvent event) {
		infoScrollPane.setViewportView(null);
		
		DefaultMutableTreeNode node = 
			(DefaultMutableTreeNode)structureTree.getLastSelectedPathComponent();
		
		if (node == null) {
			return;
		}
		
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof TreeNodeInfo) {
			TreeNodeInfo info = ((TreeNodeInfo)nodeInfo);
			Object data = info.getData();
			
			// If the node contains a LazyUILoader, replace it by the UI 
			// it loads and process the resulting JComponent
			if(data instanceof LazyUILoader) {
				info.setData(((LazyUILoader)data).createUI());
				data = info.getData();
			}
		    
			if(data instanceof JComponent) {
				infoScrollPane.setViewportView((JComponent)data);
			} 
		} 
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridx = 0;
		this.setSize(606, 405);
		this.setLayout(new GridBagLayout());
		this.add(getJSplitPane(), gridBagConstraints);
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getTreeScrollPane());
			jSplitPane.setRightComponent(getInfoScrollPane());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes treeScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTreeScrollPane() {
		if (treeScrollPane == null) {
			treeScrollPane = new JScrollPane();
			treeScrollPane.setViewportView(getStructureTree());
		}
		return treeScrollPane;
	}

	/**
	 * This method initializes structureTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getStructureTree() {
		if (structureTree == null) {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Message");
			structureTree = new JTree(root);
			
		    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		    renderer.setLeafIcon(null);
		    renderer.setOpenIcon(null);
		    renderer.setClosedIcon(null);
		    structureTree.setCellRenderer(renderer);
		    
		    structureTree.setRootVisible(false);
		    structureTree.setShowsRootHandles(true);
			structureTree.setPreferredSize(new Dimension(120, 100));
			structureTree.setMinimumSize(structureTree.getPreferredSize());
			fillTree(root, null);

			structureTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			structureTree.addTreeSelectionListener(this);
			
			structureTree.setTransferHandler(new StructureTreeTransferHandler());
		}
		return structureTree;
	}

	/**
	 * This method initializes infoScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getInfoScrollPane() {
		if (infoScrollPane == null) {
			infoScrollPane = new JScrollPane();
		}
		return infoScrollPane;
	}
	
	public void setDragEnabled(boolean dragEnabled) {
		structureTree.setDragEnabled(dragEnabled);
	}

	public boolean isDragEnabled() {
		return structureTree.getDragEnabled();
	}

	private class StructureTreeTransferHandler extends TransferHandler {

		@Override
		protected Transferable createTransferable(JComponent c) {
			if(c != structureTree)
				return null;
			
			DefaultMutableTreeNode selectedNode = getSelectedNode(structureTree); 
			TreeNodeInfo nodeInfo = (TreeNodeInfo) selectedNode.getUserObject();

			Object nodeRef = nodeInfo.getRef();
			
			try {
				if(nodeRef instanceof Message) {
					return MessageListTransferable.copyFromJMSMessage((Message)nodeRef);
				} else
				if(nodeRef instanceof JMSPart) {
					return MessageListTransferable.createFromJMSPart((JMSPart)nodeRef);
				} 
			} catch (JMSException e) {
				e.printStackTrace();
				return null;
			}
			
			return null;
		}

		@Override
		public int getSourceActions(JComponent c) {
			if(c != structureTree)
				return 0;
			
			// Only allow dragging on the content nodes of the tree
			DefaultMutableTreeNode selectedNode = getSelectedNode(structureTree); 
			TreeNodeInfo nodeInfo = (TreeNodeInfo) selectedNode.getUserObject();
			
			Object nodeRef = nodeInfo.getRef();
			
			if((nodeRef instanceof Message && !(nodeRef instanceof BytesMessage))
			|| nodeRef instanceof JMSPart)
				return COPY;
			
			return 0;
		}

		private DefaultMutableTreeNode getSelectedNode(JTree tree) {
			TreePath selection = tree.getSelectionPath();
			DefaultMutableTreeNode selectedNode = 
				(DefaultMutableTreeNode) selection.getLastPathComponent();
			return selectedNode;
		}
		
	}
	
	private interface LazyUILoader {
		public JComponent createUI();
	}
	
	private static class LazyStringLoader implements LazyUILoader {

		private final String str;
		
		public LazyStringLoader(String str) {
			this.str = str;
		}
		
		public JComponent createUI() {
			return new JLabel(str);
		}		
	}
	
	private static abstract class LazyTextAreaLoader implements LazyUILoader {
		
		protected abstract String getContent();
		
		public JComponent createUI() {
			final JTextArea textArea = new JSearchableTextArea();
			textArea.setEditable(false);
			textArea.setText(getContent());
			textArea.setCaretPosition(0);

			textArea.setToolTipText("Type to search");
			
			return textArea;
		}
	}
	
	private static abstract class LazyXmlLoader extends LazyTextAreaLoader {

		protected String formatXml(Document document) {
			try {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				StringWriter sw = new StringWriter();
				Result result = new StreamResult(sw);
				
				DOMSource source = new DOMSource(Xerces210DocumentWrapper.wrap(document));
				transformer.transform(source, result);
				
				return sw.getBuffer().toString();
			} catch (TransformerConfigurationException e) {
				return null;
			} catch (TransformerException e) {
				return null;
			}
		}
		
	}
	
	private static class LazyXmlMessageLoader extends LazyXmlLoader {
		private final TextMessage message;
		
		public LazyXmlMessageLoader(TextMessage message) {
			this.message = message;
		}
		
		@Override
		public String getContent() {
			try {
				// Try to parse the message as Xml
				if(message instanceof JMSXMLMessage) {
					// Get the Document from the message
					try {
						Document d = ((JMSXMLMessage)message).getDocument();
						return formatXml(d);
					} catch (JMSException e) {
						// Getting the Document failed, perhaps it wasn't XML after all?
						return message.getText();
					}				
				}
	
				// The message wasn't an XMLMessage, try to parse as XML anyway
				try {
					String text = message.getText();
					InputSource is = new InputSource(new StringReader(text != null?text:""));
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					db.setEntityResolver(new NullEntityResolver());
					Document doc = db.parse(is);
					
					return formatXml(doc);
				} catch (SAXException e) {
				} catch (IOException e) {
				} catch (ParserConfigurationException e) {
				}
				
				// Parsing the Xml failed, just return the text content
				return message.getText();
			} catch (JMSException e) {
				return "Exception while retrieving the contents of the message.\n" +
					e.toString();
			}
		}		
	}
	
	private static class LazyXmlPartLoader extends LazyXmlLoader {
		protected final JMSPart part;
		
		public LazyXmlPartLoader(JMSPart part) {
			this.part = part;
		}
		
		@Override
		public String getContent() {
			String content = (String)part.getContent();
			if(content != null) {
				try {
					InputSource is = new InputSource(new StringReader(content));
					Document doc = DocumentBuilderFactory.newInstance().
						newDocumentBuilder().parse(is);
					
					return formatXml(doc);
				} catch (SAXException e) {
					return content;
				} catch (IOException e) {
					return content;
				} catch (ParserConfigurationException e) {
					return content;
				}
			} else {
				return "";
			}
		}		
	}
	
	private static class LazyXmlMessagePartLoader extends LazyXmlMessageLoader {
		public LazyXmlMessagePartLoader(JMSPart part) {
			super((TextMessage)part.getContent());
		}		
	}
	
	private static class LazyMultipartMessagePartLoader implements LazyUILoader {
		protected final JMSMultipartMessage message; 
		
		public LazyMultipartMessagePartLoader(JMSPart part) {
			message = (JMSMultipartMessage)part.getContent();
		}

		public JComponent createUI() {
			MessageViewerPanel panel = new MessageViewerPanel();
			panel.setMessage(message);
			return panel;
		}		
	}

	private static abstract class LazyHexEditorLoader implements LazyUILoader {
		protected abstract byte[] getContent();
		public JComponent createUI() {
			JHexEditor hexEditor = new JHexEditor(getContent());
			hexEditor.setReadOnly(true);
			return hexEditor;
		}
	}
	
	private static class LazyBytesMessageLoader extends LazyHexEditorLoader {
		private final BytesMessage message;
		
		public LazyBytesMessageLoader(BytesMessage message) {
			this.message = message;
		}
		
		@Override
		public byte[] getContent() {
			try {
				byte[] data = new byte[(int)message.getBodyLength()];
				message.reset();
				message.readBytes(data);
				return data;
			} catch (JMSException e) {
				return null;
			}
		}
	}
	
	private static class LazyBytesPartLoader extends LazyHexEditorLoader {
		private final JMSPart part;
		
		public LazyBytesPartLoader(JMSPart part) {
			this.part = part;
		}
		
		@Override
		public byte[] getContent() {
			Object content = part.getContent();
			
			if(content != null) {
				if(byte[].class.isAssignableFrom(content.getClass())) {
					return (byte[])content;
				} else {
					return content.toString().getBytes();
				}
			} else {
				return new byte[] {};
			}
		}		
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
