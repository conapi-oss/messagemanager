package nl.queuemanager.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implements a file-backed XmlConfigurationSection by reading and writing an XML file when values are
 * changed and read. This class implements the root element (where values can be stored) and also
 * the file reading and writing.
 */
public class XmlFileConfiguration extends XmlConfigurationSection {
	private final File configFile;
	
	private final Object lock = new Object();
	
	private final DocumentBuilderFactory dbf;
	private final DocumentBuilder db;
	
	private final TransformerFactory tff;
	private final Transformer tf;
	
	public XmlFileConfiguration(File configFile, String namespaceUri, String rootElementName) {
		super(namespaceUri, rootElementName);
		
		if(configFile == null)
			throw new IllegalArgumentException("configFile");
		
		this.configFile = configFile;
		
		// Initialize the XML Parser
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unable to configure XML parser!");
		}	
		
		// Initialize the XML generator
		tff = TransformerFactory.newInstance();
		try {
			tf = tff.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Unable to configure XML generator!");
		}
	}
	
	/**
	 * Create a new, empty, configuration document.
	 * 
	 * @return
	 */
	private Document newConfig() {
		Document d = db.newDocument();
		Element configElement = d.createElementNS(namespaceUri, rootElementName);
		d.appendChild(configElement);
		return d;
	}
	
	@Override
	void mutateConfiguration(Function<? super Element, Boolean> mutateFunc) throws ConfigurationException {
		// This lock is to make sure only one thread in this process will access the
		// file at any time.
		synchronized(lock) {
			// Obtain file lock. This is to make sure multiple processes synchronize properly
			try(final FileChannel channel = FileChannel.open(configFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				final FileLock lock = channel.lock()) {

				Document configuration = readConfiguration(channel);
				Boolean changed = mutateFunc.apply(configuration.getDocumentElement());
				if(changed) {
					writeConfiguration(configuration, channel);
				}
			} catch (IOException e) {
				throw new ConfigurationException(e);
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
	}

	@Override
	<R> R readConfiguration(Function<Element, R> readFunc) throws ConfigurationException {
		// This lock is to make sure only one thread in this process will access the
		// file at any time.
		synchronized(lock) {
			try {
				try(final FileChannel channel = FileChannel.open(configFile.toPath(), StandardOpenOption.READ)) {
					Document configuration = readConfiguration(channel);
					return readFunc.apply(configuration.getDocumentElement());
				} catch (NoSuchFileException e) {
					return readFunc.apply(newConfig().getDocumentElement());
				} catch (IOException e) {
					throw new ConfigurationException(e);
				}
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
	}
	
	private Document readConfiguration(FileChannel channel) {
		try {
			final int fileSize = (int)configFile.length();
			if(fileSize == 0) {
				return newConfig();
			}
			
			final ByteBuffer buffer = ByteBuffer.allocate(fileSize);
			while(channel.read(buffer) > 0);
			
			return db.parse(new InputSource(new ByteArrayInputStream(buffer.array())));
		} catch (IOException e) {
			System.out.println("IOException getting configuration, creating new document." + e);
			e.printStackTrace();
			return newConfig();
		} catch (SAXException e) {
			System.out.println("Unable to parse configuration, creating new document." + e);
			e.printStackTrace();
			return newConfig();
		}
	}
	
	private void writeConfiguration(Document configuration, FileChannel channel) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		StreamResult r = new StreamResult(buffer);
		Source s = new DOMSource(configuration);
		
		try {
			tf.transform(s, r);
			channel.truncate(0);
			channel.write(ByteBuffer.wrap(buffer.toByteArray()));
			
			System.out.println("Written configuration:\n" + new String(buffer.toByteArray()));
		} catch (TransformerException e) {
			System.err.println("Error while saving prefs!");
			e.printStackTrace(System.err);
		}
	}

}
