package nl.queuemanager.ui.about;

import com.google.inject.Inject;
import nl.queuemanager.core.Version;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.ui.EditorPaneWithHyperlinks;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
class VersionPanel extends JPanel implements AboutPanel {

	private final CoreConfiguration config;


	@Inject
	public VersionPanel(CoreConfiguration config) {
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
				"  <h2>Message Manager</h2>\n" +
				"  <p>Version: "+ Version.VERSION+"</p>\n" +
				"\n" +
				"  <h3>Licensing Information:</h3>\n" +
				"  <ul>\n" +
				"    <li>Open Source Core/Plugins/Features: \n"+
				"     <ul><li>Apache License 2.0</li></ul></li>\n" +
				"    <li>Licensed Plugins/Features:\n"+
				"      <ul><li>Copyright (c) 2024 conapi GmbH.</li><li>All rights reserved.</li><li>Licensed under conapi GmbH terms of use.</li></ul></li>\n" +
				"  </ul>\n" +
				"  <p>Website: <a href=\"https://www.conapi.at/message-manager\">https://www.conapi.at/message-manager</a></p>\n" +
				"</div>";

				//TODO"Documentation: <a href='https://www.conapi.at/message-manager/documentation'>https://www.conapi.at/message-manager/documentation</a><br/>"+

	}

	public JComponent getUIPanel() {
		return this;
	}
	
}
