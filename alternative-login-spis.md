---
id: 122
title: Alternative Login SPIs
date: 2012-03-01T19:34:16+01:00
author: Gerco Dries
layout: page
guid: http://queuemanager.nl/?page_id=122
aktt_notify_twitter:
  - 'yes'
---
Sonic Message Manager supports two ways to customize the Login SPI class for authentication to the SonicMQ domain and brokers:

  * The system property `SonicMQ.LOGIN_SPI` is interpreted by the SonicMQ client libraries (Sonic Message Manager does not use it) and is required to use when you need the management connection to use the specified SPI. Setting this SPI will affect both management and JMS connections to the brokers.
  * The system property <tt>smm.jms.LoginSPI</tt> is needed when the management connection needs to use the default authentication, but the JMS connections need to use your custom SPI. Setting this property will not affect management connections, but only broker JMS connections.

The value for both of these properties (when set) needs to be the fully qualified classname of the client side Login SPI class. This class must be found on the classpath, which makes this method of authentication incompatible with the Web Start version of Sonic Message Manager. To use an alternative login SPI for SonicMQ authentication, like LDAP, follow these instructions:

  1. Perform an [offline installation](http://queuemanager.nl/offline-installation/ "offline installation")
  2. Copy the jars required for your SPI into the SonicMQ lib folder
  3. Create a script that performs the following actions:
  1. Build a classpath with the SonicMessageManager jar and your SPI jars. The required SonicMQ jars are referenced by the SMM jar and do not need to be specified separately
  2. Launch Sonic Message Manager with the required system properties (see above) set to the classname of your login SPI class

  4. Launch Sonic Message Manager using your script.

Example for Sonic PASS (LDAP) logins:

The only required jar file for PASS is <tt>MQPluggableAuthentication-version.jar</tt> (change version for your version) and the Login SPI class is <tt>com.sonicsw.pso.pass.client.loginspi.Login</tt> The full command line to launch SMM will be (all on one line):

<tt>java -cp </tt><tt>SonicMessageManager.jar;</tt><tt>MQPluggableAuthentication-version.jar -D<code>SonicMQ.LOGIN_SPI</code>=com.sonicsw.pso.pass.client.loginspi.Login nl.queuemanager.smm.Main</tt>

This will launch Sonic Message Manager with LDAP PASS logins enabled for both management and JMS client connections.