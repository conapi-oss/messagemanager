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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.queuemanager.core.Pair;
import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.ui.MessageListTransferable;
import nl.queuemanager.ui.util.Highlighter;
import nl.queuemanager.ui.util.HighlighterListener;
import nl.queuemanager.ui.util.HighlightsModel;
import nl.queuemanager.ui.util.ListTableModel;
import nl.queuemanager.ui.util.TreeNodeInfo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MessageViewerPanel extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JScrollPane treeScrollPane = null;
	private JTree structureTree = null;
	
	private Message message = null;
	private JPanel emptyPanel = new JPanel();

	private final SortedMap<Integer, MessageContentViewer> messageContentViewers;
	private final SortedMap<Integer, MessagePartContentViewer> partContentViewers;
	
	/**
	 * This is the default constructor
	 */
	@Inject
	public MessageViewerPanel(
		EventBus eventBus,
		Map<Integer, MessageContentViewer> messageContentViewers,
		Map<Integer, MessagePartContentViewer> partContentViewers) 
	{
		super();
		initialize();
		
		this.messageContentViewers = new TreeMap<Integer, MessageContentViewer>(messageContentViewers);
		this.partContentViewers = new TreeMap<Integer, MessagePartContentViewer>(partContentViewers);
		
		// Select the "JMS Properties" node
		structureTree.getSelectionModel().setSelectionPath(structureTree.getPathForRow(0));
		
		// FIXME Probably better to make Guice construct the JMSHeadersTable and MessagePropertiesTable and
		// subscribe them to the GlobalHighlightEvent. Then this class need not know anything about highlighters,
		// Guice or Eventbus
		eventBus.register(highlighter);
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

	private Highlighter<Pair<?, ?>> highlighter = new PairValueHighlighter();
	
	private void fillTree(DefaultMutableTreeNode root, Message message) {
		DefaultTreeModel model = (DefaultTreeModel) structureTree.getModel();
		
		root.removeAllChildren();
		JMSHeadersTable messagePropertiesTable = new JMSHeadersTable();
		messagePropertiesTable.setHighlightsModel(HighlightsModel.with(
				(ListTableModel<? extends Pair<?, ?>>) messagePropertiesTable.getModel(), highlighter));
		messagePropertiesTable.setMessage(this.message);
		root.add(
			new DefaultMutableTreeNode(
				new TreeNodeInfo(
					"JMS Headers", 
					new JScrollPane(messagePropertiesTable))));
		
		MessagePropertiesTable messageHeadersTable = new MessagePropertiesTable();
		messageHeadersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		messageHeadersTable.setHighlightsModel(HighlightsModel.with(
				(ListTableModel<? extends Pair<?, ?>>) messageHeadersTable.getModel(), highlighter));
		messageHeadersTable.setMessage(this.message);
		root.add(
			new DefaultMutableTreeNode(
				new TreeNodeInfo(
					"Properties", 
					new JScrollPane(messageHeadersTable))));
		
		if(message != null) try {
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
						ContentViewer<JMSPart> viewer = getContentViewer(partContentViewers, part); 
						partNode = new DefaultMutableTreeNode(new TreeNodeInfo(
							"Part " + i + " (" + viewer.getDescription(part) + ")", viewer, part));
						root.add(partNode);
						
						MessagePartHeadersTable partHeadersTable = new MessagePartHeadersTable();
						partHeadersTable.setMessagePart(part);
						partNode.add(new DefaultMutableTreeNode(new TreeNodeInfo("Headers", partHeadersTable)));
					}
				}
			} else {
				ContentViewer<Message> viewer = getContentViewer(messageContentViewers, message);
				
				if(viewer != null) {
					root.add(new DefaultMutableTreeNode(new TreeNodeInfo(
						"Body (" + viewer.getDescription(message) + ")", viewer, message)));
				} else {
					root.add(new DefaultMutableTreeNode(new TreeNodeInfo(
						"Unknown content", new StringContentViewer(), 
							"Unable to display content of " + message.getClass().getSimpleName())));
				}
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

	/**
	 * Interrogate each ContentViewer<T> in the contentViewers map in the natural order of the Map
	 * and return the first ContentViewer<T> that supports the given object.
	 * 
	 * @param <C>
	 * @param <T>
	 * @param contentViewers
	 * @param object
	 * @return
	 */
	private <C, T> ContentViewer<T> getContentViewer(Map<C, ? extends ContentViewer<T>> contentViewers, T object) {
		// FIXME This uses the maps natural ordering. This may not be what we want!
		for(Map.Entry<C, ? extends ContentViewer<T>> entry: contentViewers.entrySet()) {
			ContentViewer<T> v = entry.getValue();
			if(v.supports(object))
				return v;
		}
		
		return null;
	}

	public void valueChanged(TreeSelectionEvent event) {
		DefaultMutableTreeNode node = 
			(DefaultMutableTreeNode)structureTree.getLastSelectedPathComponent();
		
		if (node == null) {
			return;
		}
		
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof TreeNodeInfo) {
			TreeNodeInfo info = ((TreeNodeInfo)nodeInfo);
			Object data = info.getData();
			
			// If the node contains a ContentViewer, replace it by the UI 
			// it loads and process the resulting JComponent
			if(data instanceof MessageContentViewer) {
				info.setData(((MessageContentViewer)data).createUI((Message)info.getRef()));
				data = info.getData();
			}
			if(data instanceof MessagePartContentViewer) {
				info.setData(((MessagePartContentViewer)data).createUI((JMSPart)info.getRef()));
				data = info.getData();
			}
		    
			if(data instanceof JComponent) {
				int location = jSplitPane.getDividerLocation();
				jSplitPane.setRightComponent((JComponent)data);
				jSplitPane.setDividerLocation(location);
				jSplitPane.revalidate();
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
			jSplitPane.setRightComponent(emptyPanel);
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
	
	public void setDragEnabled(boolean dragEnabled) {
		structureTree.setDragEnabled(dragEnabled);
	}

	public boolean isDragEnabled() {
		return structureTree.getDragEnabled();
	}

	@SuppressWarnings("serial")
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
		
}  
