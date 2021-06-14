module messagemanager.app {
    exports nl.queuemanager.app;
    exports nl.queuemanager.app.tasks;

    opens nl.queuemanager.app.tasks;

    uses com.google.inject.Module;

    requires messagemanager.ui;
    requires messagemanager.core;
    requires messagemanager.plugin.api;
    requires messagemanager.jmsmessages;

    requires jakarta.jms.api;
    requires com.google.common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    requires static lombok;

    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires java.xml;
    requires javax.inject;
}