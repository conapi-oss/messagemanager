package nl.queuemanager.ui;

import com.google.common.eventbus.Subscribe;
import nl.queuemanager.core.util.ReleasePropertiesEvent;
import nl.queuemanager.ui.util.MarqueePanel;

import jakarta.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MOTDPanel extends MarqueePanel {

	@Inject
	public MOTDPanel() {
		super(10, 5);
		
		//setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(Color.decode("#a4d600"));
		setForeground(Color.BLACK);
		
		// This panel starts out invisible. If there is an MOTD, it will be made visible.
		setVisible(false);
		
		setScrollWhenFocused(false);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setToolTipText("Right-click to hide this message");
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					setVisible(false);
				}
			}
		});
	}

	public void addMessage(String message) {
		JLabel label = new JLabel(message);
		label.setForeground(Color.BLACK);
		add(label);
		add(Box.createHorizontalStrut(25));
		setVisible(true);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			startScrolling();
		} else {
			stopScrolling();
		}
		
		super.setVisible(visible);
	}
	
	@Subscribe
	public void processEvent(final ReleasePropertiesEvent event) {
		if(SwingUtilities.isEventDispatchThread()) {
			switch(event.getId()) {
			case RELEASE_NOTES_FOUND:
			case MOTD_FOUND:
				addMessage(event.getInfo().toString());
				break;
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					processEvent(event);
				}
			});
		}
	}

}
