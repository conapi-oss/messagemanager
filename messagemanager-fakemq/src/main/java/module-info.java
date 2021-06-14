import nl.queuemanager.fakemq.FakeMQConnectivityProvider;
import nl.queuemanager.fakemq.FakeMQModule;

module messagemanager.fakemq {
    exports nl.queuemanager.fakemq;

    provides com.google.inject.Module with FakeMQModule;

    requires messagemanager.core;
    requires messagemanager.jmsmessages;
    requires messagemanager.plugin.api;
    requires messagemanager.ui;

    requires jakarta.jms.api;
    requires com.google.common;
    requires com.google.guice;

    requires java.desktop;
    requires java.management;
    requires javax.inject;
}