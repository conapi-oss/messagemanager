module messagemanager.app {
    exports nl.queuemanager.app;
    exports nl.queuemanager.app.tasks;

    opens nl.queuemanager.app.tasks;

    requires messagemanager.ui;
    requires messagemanager.core;
    requires messagemanager.plugin.api;
    requires messagemanager.jmsmessages;

    requires com.formdev.flatlaf;
    requires jakarta.jms.api;
    requires com.google.common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    uses com.google.inject.Module;
    requires static lombok;

    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires java.xml;
    requires javax.inject;
}