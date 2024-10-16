package nl.queuemanager.ui.about;

import com.google.inject.Inject;
import nl.queuemanager.ui.EditorPaneWithHyperlinks;
import nl.queuemanager.core.configuration.CoreConfiguration;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
class SupportPanel extends JPanel implements AboutPanel {

	private final CoreConfiguration config;


	@Inject
	public SupportPanel(CoreConfiguration config) {
		setAlignmentY(Component.TOP_ALIGNMENT);
		this.config = config;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		// Show HTML in a EditorPaneWithHyperlinks component with a padding of 10 pixels
		JEditorPane editorPane = new EditorPaneWithHyperlinks(getHTML());

	//	JScrollPane scrollPane = new JScrollPane(editorPane);
	//	scrollPane.setPreferredSize(new Dimension(400, 300));
	//	scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		add(editorPane);
	}

	private String getHTML() {
		return "<div>\n" +
				"  If you have an active license (including Evaluation licenses), <br/>you can contact the CONAPI Support team with questions, feature requests, or bug reports.</p>\n" +
				"  \n" +
				"  <br>Please use the following contact information:\n" +
				"  \n" +
				"  <ul>\n" +
				"    <li>Email: <a href=\"mailto:support@conapi.at\">support@conapi.at</a></li>\n" +
				"    <li>Web: <a href=\"https://www.conapi.at/support/\">https://www.conapi.at/support/</a></li>\n" +
				"  </ul>\n" +
				"</div>";
	}

	public JComponent getUIPanel() {
		return this;
	}
	
}
