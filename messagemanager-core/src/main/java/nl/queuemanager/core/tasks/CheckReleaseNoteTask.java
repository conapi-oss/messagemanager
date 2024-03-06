package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.DebugProperty;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.core.util.DNSUtil;
import nl.queuemanager.core.util.ReleasePropertiesEvent;
import nl.queuemanager.core.util.ReleasePropertiesEvent.EVENT;

import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * <p>
 * Retrieve release note from home base. The release note is encoded in the TXT DNS records of 
 * *.relnote.queuemanager.nl. The reasons for this are as follows:
 * </p><p>
 * <ol>
 * <li>We can easily get this information from behind proxy servers;</li>
 * <li>If we are behind a proxy, there should be no authentication dialog popping up;</li>
 * <li>The information will be cached for free by any corporate/ISP DNS servers.</li>
 * </ol>
 * </p>
 **/
public class CheckReleaseNoteTask extends BackgroundTask {
	private final Logger log = Logger.getLogger(getClass().getName());
	private final CoreConfiguration config;
	private final String hostname;
	private final String buildId;

	@Inject
	public CheckReleaseNoteTask(CoreConfiguration config, EventBus eventBus, @Assisted("hostname") String hostname, @Assisted("buildId") String buildId) {
		super(null, eventBus);
		this.config = config;
		this.hostname = hostname;
		this.buildId = buildId;
	}

	@Override
	public void execute() throws Exception {
		if(shouldShowReleaseNote()) {
			// Get the release note and show it
			String note = DNSUtil.getFirstTxtRecord(buildId + ".relnote." + hostname);
			if(note != null && note.trim().length() != 0) {
				eventBus.post(new ReleasePropertiesEvent(EVENT.RELEASE_NOTES_FOUND, this, note));
			}
		}

		// Update the last run build number
		config.setUserPref(CoreConfiguration.PREF_LAST_RUN_BUILD, buildId);
	}
		
	private boolean shouldShowReleaseNote() {
		// If the check is forced, always do it
		if(DebugProperty.forceReleaseNoteCheck.isEnabled()) {
			log.warning("Release note check forced by system property");
			return true;
		}

		// Check if we've run this build before. If we have not, show the release note
		String lastBuild = config.getUserPref(CoreConfiguration.PREF_LAST_RUN_BUILD, "");
		if(!buildId.equals(lastBuild)) {
			log.info(String.format("This is the first time build %s was launched, show release note", buildId));
			return true;
		}
		
		return false;
	}
	
}