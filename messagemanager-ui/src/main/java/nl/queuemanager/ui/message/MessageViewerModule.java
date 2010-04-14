package nl.queuemanager.ui.message;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class MessageViewerModule extends AbstractModule {

	@Override
	protected void configure() {
		// TODO Allow for more content viewers
		
		// Register all message content viewers
		Multibinder<MessageContentViewer> mcv = 
			Multibinder.newSetBinder(binder(), MessageContentViewer.class);
		mcv.addBinding().to(BytesMessageViewer.class);
		mcv.addBinding().to(XmlMessageContentViewer.class);
		
		// Register all message part content viewers
		Multibinder<MessagePartContentViewer> mpv = 
			Multibinder.newSetBinder(binder(), MessagePartContentViewer.class);
		mpv.addBinding().to(BytesPartViewer.class);
		mpv.addBinding().to(XmlPartContentViewer.class);
		mpv.addBinding().to(XmlMessagePartContentViewer.class);
		mpv.addBinding().to(MultipartMessagePartContentViewer.class);
	}

}
