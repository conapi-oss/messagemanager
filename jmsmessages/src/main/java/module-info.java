module messagemanager.jmsmessages {
   exports nl.queuemanager.jms;
   exports nl.queuemanager.jms.impl;

   requires java.activation;
   requires java.datatransfer;
   requires java.xml;
   requires javax.jms.api;
   requires static lombok;
}