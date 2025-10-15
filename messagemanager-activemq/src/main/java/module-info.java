import nl.queuemanager.activemq.ActiveMQModule;

module messagemanager.activemq {
    exports nl.queuemanager.activemq;

    provides com.google.inject.Module with ActiveMQModule;

    opens nl.queuemanager.activemq to com.google.guice;
    opens nl.queuemanager.activemq.ui;
    opens nl.queuemanager.activemq.incompat;

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
    requires jakarta.inject;
    requires java.logging;
    requires static jdk.attach; //static as only a JDK will have it

    requires javax.jms.api;
    requires static activemq.client;

    //requires java.activation;

    // below are automatic module names as ActiveMQ JARs have no module-info
    //requires static smc;
}