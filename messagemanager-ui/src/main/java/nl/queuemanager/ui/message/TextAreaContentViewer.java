package nl.queuemanager.ui.message;

import com.google.common.eventbus.Subscribe;
import nl.queuemanager.ui.GlobalHighlightEvent;
import nl.queuemanager.ui.util.JSearchableTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

abstract class TextAreaContentViewer<T> implements ContentViewer<T> {
	
	protected abstract String getContent(T object);
	
	private RSyntaxTextArea textArea;
	
	public RTextScrollPane createUI(T object) {
		textArea = createTextArea(object);
		return new RTextScrollPane(textArea);
	}

	protected RSyntaxTextArea createTextArea(T object) {
		final RSyntaxTextArea textArea = new JSearchableTextArea();
		textArea.setText(getContent(object));
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		return textArea;
	}
	
	@Subscribe
	public void onGlobalHighlightEvent(GlobalHighlightEvent e) {
		SearchContext context = new SearchContext();
		context.setSearchFor(e.getHighlightString());
		context.setMatchCase(false);
		context.setRegularExpression(false);
		context.setSearchForward(true);
		context.setWholeWord(false);
		SearchEngine.markAll(textArea, context);
	}
	
}
