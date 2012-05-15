package nl.queuemanager.ui.message;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class MessageViewerModule extends AbstractModule {

	@Override
	protected void configure() {
		// TODO Allow for dynamically registered content viewers

		// Create the MapBinders for the viewer registry
		MapBinder<Integer, MessageContentViewer> mcv = 
			MapBinder.newMapBinder(binder(), Integer.class, MessageContentViewer.class);
		MapBinder<Integer, MessagePartContentViewer> mpv = 
			MapBinder.newMapBinder(binder(), Integer.class, MessagePartContentViewer.class);
		
		// Register all message content viewers
		mcv.addBinding(10).to(TextMessageContentViewer.class);
		mcv.addBinding(20).to(MapMessageContentViewer.class);
		mcv.addBinding(Integer.MAX_VALUE).to(BytesMessageViewer.class);
		
		// Register all message part content viewers
		mpv.addBinding(10).to(TextPartContentViewer.class);
		mpv.addBinding(Integer.MAX_VALUE).to(BytesPartViewer.class);
	}

}
