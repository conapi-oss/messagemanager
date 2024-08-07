package nl.queuemanager.ui.util;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class MMJTable extends JTable {

	protected HighlightsModel<?> highlightsModel;
	private Color highlightColor = Color.decode("#a4d600");


	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component c = super.prepareRenderer(renderer, row, column);
	    if (!isRowSelected(row) ) {
			// Convert view row to model row
			int modelRow = convertRowIndexToModel(row);

	        c.setBackground(isHighlighted(modelRow) ? getHighlightColor() : getBackground());
			c.setForeground(isHighlighted(modelRow) ? Color.BLACK : getForeground());
	    } else {
	    	c.setBackground(getSelectionBackground());
			c.setForeground(getSelectionForeground());
	    }
	    return c;
	}

	protected boolean isHighlighted(int row) {
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