package nl.queuemanager.ui.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class MMJTable extends JTable {

	private HighlightsModel<?> highlightsModel;

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component c = super.prepareRenderer(renderer, row, column);
	    if (!isRowSelected(row) ) {
	        c.setBackground(isHighlighted(row) ? Color.GREEN : getBackground());
	    } else {
	    	c.setBackground(getSelectionBackground());
	    }
	    return c;
	}

	private boolean isHighlighted(int row) {
		return highlightsModel != null && highlightsModel.isHighlighted(row);
	}

	public void setHighlightsModel(HighlightsModel<?> model) {
		highlightsModel = model;
	}

}