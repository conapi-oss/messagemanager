package nl.queuemanager.ui.util;

import com.google.common.base.Strings;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JFileField extends JTextField {
	public JFileField() {
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DTL());
	}

	public File getFile() {
		if (Strings.isNullOrEmpty(getText()))
			return null;
		return new File(getText());
	}

	public void setFile(File file) {
		if (file != null) {
			setText(file.getAbsolutePath());
		} else {
			setText("");
		}
		if(getInputVerifier() != null) {
			getInputVerifier().verify(this);
		}
	}

	private class DTL extends DropTargetAdapter {
		public void drop(DropTargetDropEvent dtde) {
			if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && canImport(dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				dtde.dropComplete(importData(dtde.getTransferable()));
				dragExit(null);
			} else {
				dtde.rejectDrop();
				dragExit(null);
			}
		}

		private boolean importData(Transferable transferable) {
			try {
				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
					if(!files.isEmpty()) {
						return importData(files.get(0));
					}
				}
				
				if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					return importData((String) transferable.getTransferData(DataFlavor.stringFlavor));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			}

			return false;
		}

		private boolean importData(String transferData) {
			setFile(new File(transferData));
			return true;
		}

		private boolean importData(File transferData) {
			setFile(transferData);
			return true;
		}

		private boolean canImport(DataFlavor[] currentDataFlavors) {
			for (DataFlavor flavor : currentDataFlavors) {
				if (DataFlavor.javaFileListFlavor.equals(flavor))
					return true;

				if (DataFlavor.stringFlavor.equals(flavor))
					return true;
			}

			return false;
		}
	}

}