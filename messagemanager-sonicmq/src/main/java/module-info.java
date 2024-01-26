import nl.queuemanager.smm.SMMPluginModule;

module messagemanager.sonicmq {
    exports nl.queuemanager.smm;

    provides com.google.inject.Module with SMMPluginModule;

    opens nl.queuemanager.smm to com.google.guice;

    requires messagemanager.core;
    requires messagemanager.jmsmessages;
    requires messagemanager.plugin.api;
    requires messagemanager.ui;


    requires com.google.common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    requires static lombok;

    requires java.desktop;
    requires java.management;
    requires javax.inject;
    requires java.logging;

    requires jakarta.jms.api;
    requires java.activation;

    requires static soniclibs; // static or remove?
}