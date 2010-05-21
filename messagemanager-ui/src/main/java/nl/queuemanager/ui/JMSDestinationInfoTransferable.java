package nl.queuemanager.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.queuemanager.jms.JMSDestination;

public class JMSDestinationInfoTransferable implements Transferable {
	
	public static DataFlavor jmsDestinationInfosFlavor = 
		new DataFlavor(JMSDestinationInfo[].class, "application/x-smm-jmsdestinationinfos");
	private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] {jmsDestinationInfosFlavor, DataFlavor.stringFlavor};
	private static String newline = System.getProperty("line.separator");

	private final JMSDestinationInfo[] destinationInfos;
	
	public JMSDestinationInfoTransferable(JMSDestination[] destinations) {
		List<JMSDestinationInfo> infos = new ArrayList<JMSDestinationInfo>();
		
		for(JMSDestination destination: destinations) {
			infos.add(new JMSDestinationInfo(destination));
		}
		
		destinationInfos = infos.toArray(new JMSDestinationInfo[infos.size()]);
	}
	
	public JMSDestinationInfoTransferable(Collection<? extends JMSDestination> destinations) {
		this(destinations.toArray(new JMSDestination[destinations.size()]));
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if(jmsDestinationInfosFlavor.equals(flavor))
			return destinationInfos;
		
		if(DataFlavor.stringFlavor.equals(flavor)) {
			if(destinationInfos.length == 1)
				return destinationInfos[0].getName();
			
			StringBuffer sb = new StringBuffer();
			for(JMSDestinationInfo dst: destinationInfos) {
				sb.append(dst.getName());
				sb.append(newline);
			}
			
			return sb.toString();
		}
		
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return DATA_FLAVORS;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for(DataFlavor f: DATA_FLAVORS)
			if(f.equals(flavor)) 
				return true;
		
		return false;
	}

}
