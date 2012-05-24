package nl.queuemanager.smm.ui;

import nl.queuemanager.ui.util.DesktopHelper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

public class ConnectionDialogProvider implements Provider<JDomainConnectionDialog> {
	private final Injector injector;
	private final DesktopHelper desktop;
	
	@Inject
	public ConnectionDialogProvider(Injector injector, DesktopHelper desktop) {
		this.injector = injector;
		this.desktop = desktop;
	}

	public JDomainConnectionDialog get() {
		JDomainConnectionDialog dialog = new JDomainConnectionDialog(injector.getInstance(SMMFrame.class));
		desktop.makeMacSheet(dialog);
		return dialog;
	}
	
}
