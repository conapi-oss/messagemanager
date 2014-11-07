package nl.queuemanager.smm.ui;

import nl.queuemanager.ui.util.DesktopHelper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

/**
 * This provider solves the circular dependency that exists when we attempt to inject a
 * JDomainConnectionDialog directly into a UITab. SMMFrame requires the UITab objects
 * and if one of them also requires SMMFrame, injection fails. We use this provider
 * to delay resolving the SMMFrame dependency until it's needed and thereby resolve
 * the circular dependency.
 */
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
