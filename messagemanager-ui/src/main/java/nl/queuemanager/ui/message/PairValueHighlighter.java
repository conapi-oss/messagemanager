package nl.queuemanager.ui.message;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

import nl.queuemanager.core.Pair;
import nl.queuemanager.ui.GlobalHighlightEvent;
import nl.queuemanager.ui.util.HighlighterSupport;

public final class PairValueHighlighter extends HighlighterSupport<Pair<?, ?>> {
	private String searchTerm = "";

	@Subscribe
	public void onGlobalHighlightEvent(GlobalHighlightEvent e) {
		searchTerm = e.getHighlightString();
		resetHighlights();
	}

	@Override
	public boolean shouldHighlight(Pair<?, ?> obj) {
		return obj != null && !Strings.isNullOrEmpty(searchTerm) && obj.toString().toLowerCase().contains(searchTerm.toLowerCase());
	}
}