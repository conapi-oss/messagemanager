package nl.queuemanager.app;

import nl.queuemanager.Profile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * When an update brings a plugin with a newer client library, the profile stored from the earlier version of that
 * plugin must move to the new client folder - unless the user changed it on purpose, or the new jars are not where
 * the app would load them from.
 */
public class ProfileManagerTest {
	private static final String KAFKA_PROFILE = "52239f78-79d6-11e5-a560-6831c1x20182";
	private static final String KAFKA_MODULE = "at.conapi.kafka.KafkaModule";
	private static final Predicate<URL> EVERY_JAR_THERE = url -> true;
	private static final Predicate<URL> NO_JAR_THERE = url -> false;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void anUnchangedBuiltInProfileTakesThePluginsNewClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
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
	public void aBuiltInProfileWithAnOutsideJarTakesTheNewClientAndKeepsTheOutsideJar() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/Users/someone/libs/extra-serializer.jar"));

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(List.of("file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:/Users/someone/libs/extra-serializer.jar"), urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void outsideJarsKeepTheirOrderAfterTheNewClientJars() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/Users/someone/libs/first.jar",
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/Users/someone/libs/second.jar"));

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar"), true);

		assertEquals(List.of("file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:/@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar",
				"file:/Users/someone/libs/first.jar",
				"file:/Users/someone/libs/second.jar"), urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aProfileWithAnOutsideJarIsUntouchedWhileTheClientIsUnchanged() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		Profile current = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/Users/someone/libs/extra-serializer.jar",
				"file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar");
		List<String> before = urls(current);
		manager.putProfileIfNotExist(current);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aProfileOnAnotherClientVersionThanThePluginShipsIsMarked() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		Profile pinned = profile("users-kafka-3.7-profile", List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		manager.putProfileIfNotExist(pinned);
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertTrue(manager.namesAnotherClientVersion(manager.getProfileById("users-kafka-3.7-profile")));
		assertFalse(manager.namesAnotherClientVersion(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aProfileWithItsOwnCopyOfAnOlderClientJarIsMarked() throws Exception {
		// e.g. a profile made before the clients shipped with the plugins, naming the user's own client jars
		assertTrue(markedAfterThePluginIsSeen("file:/Users/someone/kafka/kafka-clients-3.7.0.jar"));
	}

	@Test
	public void aProfileWithItsOwnCopyOfTheShippedVersionIsNotMarked() throws Exception {
		assertFalse(markedAfterThePluginIsSeen("file:/opt/kafka/kafka-clients-3.9.2.jar"));
	}

	@Test
	public void aProfileWithUnrelatedOwnJarsIsNotMarked() throws Exception {
		assertFalse(markedAfterThePluginIsSeen("file:/Users/someone/libs/extra-serializer.jar"));
	}

	@Test
	public void anOwnCopyOfAShippedJarIsDroppedWhenTheProfileMoves() throws Exception {
		// it would replace the plugin's jar of the same module name in the plugin's module layer
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/Users/someone/libs/lz4-java-1.8.0.jar",
				"file:/Users/someone/libs/extra-serializer.jar"));

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar"), true);

		assertEquals(List.of("file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:/@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar",
				"file:/Users/someone/libs/extra-serializer.jar"), urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void nothingIsMarkedBeforeThePluginsProfileIsSeen() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		Profile pinned = profile("users-kafka-3.7-profile", List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		manager.putProfileIfNotExist(pinned);

		assertFalse(manager.namesAnotherClientVersion(pinned));
	}

	@Test
	public void aProfileTheUserMadeOnThePluginKeepsItsPinnedClientLibraries() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
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
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		Profile mixed = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar",
				"file:/@MM_HOME@/clients/kafka/3.6.0/lz4-java-1.8.0.jar");
		List<String> before = urls(mixed);
		manager.putProfileIfNotExist(mixed);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aJarTheUserRemovedStaysRemovedWhileTheClientIsUnchanged() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		// the app finds the same plugin at every start: no update, so nothing to move
		Profile trimmed = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar");
		List<String> before = urls(trimmed);
		manager.putProfileIfNotExist(trimmed);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aLibraryRaisedInsideTheSameClientFolderReachesTheStoredProfile() throws Exception {
		// a release raises a vulnerable library under an unchanged client: the folder keeps its name
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:/@MM_HOME@/clients/kafka/3.9.2/snappy-java-1.1.10.12.jar"));

		Profile fromPlugin = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/snappy-java-1.1.10.13.jar");
		manager.putProfileIfNotExist(fromPlugin, true);

		assertEquals(urls(fromPlugin), urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aBuiltInProfileStaysOnItsClientWhileOnlySomeNewJarsArePresent() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"),
				url -> url.toString().contains("kafka-clients"));
		Profile working = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		List<String> before = urls(working);
		manager.putProfileIfNotExist(working);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar",
				"file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aPluginProfileWithoutClasspathLeavesTheStoredOneAlone() throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		Profile working = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		List<String> before = urls(working);
		manager.putProfileIfNotExist(working);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar")), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void aBuiltInProfileStaysOnItsClientWhileTheNewJarsAreMissing() throws Exception {
		// e.g. an update that did not finish, or an installation whose @MM_HOME@ the updates do not reach
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), NO_JAR_THERE);
		Profile working = profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:/@MM_HOME@/clients/kafka/3.7.0/kafka-clients-3.7.0.jar");
		List<String> before = urls(working);
		manager.putProfileIfNotExist(working);

		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);

		assertEquals(before, urls(manager.getProfileById(KAFKA_PROFILE)));
	}

	@Test
	public void isOnDiskResolvesMmHomeAsThePluginLoaderDoes() throws Exception {
		File mmHome = folder.newFolder("mm home");
		File jar = new File(mmHome, "clients/kafka/3.9.2/kafka-clients-3.9.2.jar");
		assertTrue(jar.getParentFile().mkdirs() && jar.createNewFile());

		String home = mmHome.getAbsolutePath();
		assertTrue(ProfileManager.isOnDisk(new URL("file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), home));
		assertTrue(ProfileManager.isOnDisk(new URL("file:/@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), home));
		assertFalse(ProfileManager.isOnDisk(new URL("file:///@MM_HOME@/clients/kafka/3.9.2/lz4-java-1.11.4.jar"), home));
		assertFalse(ProfileManager.isOnDisk(new URL("file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), null));
	}

	/** A profile the user made with one entry, after the Kafka plugin's profile (shipping 3.9.2) was seen. */
	private boolean markedAfterThePluginIsSeen(String entry) throws Exception {
		ProfileManager manager = new ProfileManager(folder.newFolder("profiles"), EVERY_JAR_THERE);
		manager.putProfileIfNotExist(profile("users-kafka", List.of("kafka-clients-*.jar"), entry));
		manager.putProfileIfNotExist(profile(KAFKA_PROFILE, List.of("kafka-clients-*.jar"),
				"file:///@MM_HOME@/clients/kafka/3.9.2/kafka-clients-3.9.2.jar"), true);
		return manager.namesAnotherClientVersion(manager.getProfileById("users-kafka"));
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
