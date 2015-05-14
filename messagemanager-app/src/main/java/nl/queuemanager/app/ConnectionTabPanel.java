package nl.queuemanager.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.Main;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UIModule;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ConnectionTabPanel extends JPanel implements UITab {

	private final Injector parentInjector;
	private final EventBus eventBus;
	
	@Inject
	public ConnectionTabPanel(final Injector injector, final EventBus eventBus) {
		this.parentInjector = injector;
		this.eventBus = eventBus;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Add the buttons
		JButton activeMQ = new JButton("Click me for activemq");
		activeMQ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					initializeWithModule("nl.queuemanager.activemq.ActiveMQModule", new URL[] {
						new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-activemq/target/messagemanager-activemq-3.0-SNAPSHOT.jar"),
						new URL("file:///Users/gerco/Projects/Technekes/apache-activemq-5.10.0/activemq-all-5.10.0.jar")
					});
				} catch (MalformedURLException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		add("ActiveMQ", activeMQ);
		
		JButton sonicMQ = new JButton("Click me for sonicmq");
		sonicMQ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File dir = new File("/Users/gerco/Projects/MessageManager/workspace/messagemanager-2.x/lib/8.6");
					File[] files = dir.listFiles();
					List<URL> urls = new ArrayList<URL>(files.length);
					urls.add(new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-sonicmq/target/messagemanager-sonicmq-3.0-SNAPSHOT.jar"));
					for(File file: files) {
						urls.add(file.toURL());
					}
					initializeWithModule("nl.queuemanager.smm.SMMModule", urls.toArray(new URL[urls.size()]));
				} catch (MalformedURLException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		add("SonicMQ", sonicMQ);
	}

	private void initializeWithModule(String moduleName, URL[] urls) {
		Module module = Main.loadModule(moduleName, urls);
		final Injector injector = parentInjector.createChildInjector(new CoreModule(), new UIModule(), module);
		ConnectivityProviderPlugin provider = injector.getInstance(ConnectivityProviderPlugin.class);
		provider.initialize();
	}
	
	public String getUITabName() {
		return "Connection";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
			//ConnectionState.CONNECTED,
			ConnectionState.DISCONNECTED
		};
	}

}
