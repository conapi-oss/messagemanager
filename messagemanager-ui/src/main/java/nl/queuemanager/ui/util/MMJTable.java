package nl.queuemanager.ui.util;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class MMJTable extends JTable {

	private HighlightsModel<?> highlightsModel;
	private Color highlightColor = Color.YELLOW;

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component c = super.prepareRenderer(renderer, row, column);
	    if (!isRowSelected(row) ) {
	        c.setBackground(isHighlighted(row) ? getHighlightColor() : getBackground());
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

	public Color getHighlightColor() {
		return highlightColor;
	}

	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
	}

}