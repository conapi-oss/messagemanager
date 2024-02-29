import nl.queuemanager.smm.SMMPluginModule;

module messagemanager.sonicmq {
    exports nl.queuemanager.smm;

    provides com.google.inject.Module with SMMPluginModule;

    opens nl.queuemanager.smm to com.google.guice;
    opens nl.queuemanager.smm.ui;

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

    // below are automatic module names as Sonic JARs have no module-info
    requires static smc;
    //requires static sonic.SSL;
    requires static sonic.Client;
    requires static sonic.mgmt.client;
    //requires static sonic.Selector;
    requires static sonic.Crypto;
   // requires static mail;
    requires static mgmt.client;
    requires static sonic.XMessage;
    //requires static sonic.Client.ext;
    //requires static sonic.ASPI;
    requires static mgmt.config;
    requires static xercesImpl;
}