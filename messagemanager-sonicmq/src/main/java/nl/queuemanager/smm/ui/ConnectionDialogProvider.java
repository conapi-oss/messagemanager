package nl.queuemanager.smm.ui;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;
import nl.queuemanager.ui.util.DesktopHelper;

/**
 * This provider solves the circular dependency that exists when we attempt to inject a
 * JDomainConnectionDialog directly into a UITab. SMMFrame requires the UITab objects
 * and if one of them also requires SMMFrame, injection fails. We use this provider
 * to delay resolving the SMMFrame dependency until it's needed and thereby resolve
 * the circular dependency.
 */
public class ConnectionDialogProvider implements Provider<JDomainConnectionDialog> {
	private final DesktopHelper desktop;
	
	@Inject
	public ConnectionDialogProvider(DesktopHelper desktop) {
		this.desktop = desktop;
	}

	public JDomainConnectionDialog get() {
		JDomainConnectionDialog dialog = new JDomainConnectionDialog(null);
		desktop.makeMacSheet(dialog);
		return dialog;
	}
	
}
