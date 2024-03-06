open module messagemanager.core {
    exports nl.queuemanager.core.configuration;
    exports nl.queuemanager.core.events;
    exports nl.queuemanager.core.jms;
    exports nl.queuemanager.core.platform;
    exports nl.queuemanager.core.task;
    exports nl.queuemanager.core.tasks;
    exports nl.queuemanager.core.util;
    exports nl.queuemanager.core;

    requires messagemanager.jmsmessages;

    requires jakarta.jms.api;
    requires com.google.common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    requires static lombok;

    requires java.desktop;
    requires java.naming;
    requires java.xml;
    requires java.logging;
    //requires javax.inject;
    requires jakarta.inject;
    requires jsr305;
}