package nl.queuemanager.app;

import nl.queuemanager.Profile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * When the app finds a plugin, the profile stored from an earlier version of that plugin must pick up the plugin's
 * current client libraries - unless the user changed them on purpose.
 */
public class ProfileManagerTest {
	private static final String KAFKA_PROFILE = "52239f78-79d6-11e5-a560-6831c1x20182";
	private static final String KAFKA_MODULE = "at.conapi.kafka.KafkaModule";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void anUnchangedBuiltInProfileTakesThePluginsNewClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"));
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/@MM_HOME@/clients/kafka/3.7.0/lz4-java-1.8.0.jar"));

		Profile fromPlugin = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar", "zstd-jni-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar");
		manager.putProfileIfNotExist(fromPlugin, true);

		Profile stored = manager.getProfileById(KAFKA_PROFILE);
		assertEquals(urls(fromPlugin), urls(stored));
		assertEquals(fromPlugin.getJars(), stored.getJars());
	}

	@Test
	public void aBuiltInProfileTheUserEditedKeepsItsClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"));
		Profile edited = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/Users/someone/libs/extra-serializer.jar");
		List<String> before = urls(edited);
		manager.putProfileIfNotExist(edited);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aProfileTheUserMadeOnThePluginKeepsItsPinnedClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"));
		// a copy of the built-in profile, pinned to the old client on purpose - the generated shape, its own id
		Profile pinned = profile("users-kafka-3.7-profile", List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		List<String> before = urls(pinned);
		manager.putProfileIfNotExist(pinned);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById("users-kafka-3.7-profile")));
	}

	@Test
	public void aBuiltInProfileSpanningTwoClientFoldersKeepsItsClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"));
		Profile mixed = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/@MM_HOME@/clients/kafka/3.6.0/lz4-java-1.8.0.jar");
		List<String> before = urls(mixed);
		manager.putProfileIfNotExist(mixed);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	private static Profile profile(String id, List<String> jars, String... classpath) throws MalformedURLException {
		Profile p = new Profile();
		p.setId(id);
		p.setName("Apache Kafka");
		p.setPlugins(new ArrayList<>(List.of(KAFKA_MODULE)));
		p.setJars(new ArrayList<>(jars));
		List<URL> urls = new ArrayList<>();
		for (String entry : classpath) {
			urls.add(new URL(entry));
		}
		p.setClasspath(urls);
		return p;
	}

	// URL.equals resolves hosts; compare the text instead
	private static List<String> urls(Profile p) {
		return p.getClasspath().stream().map(URL::toString).collect(Collectors.toList());
	}
}
