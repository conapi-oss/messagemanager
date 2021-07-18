---
id: 28
title: Offline installation
date: 2009-02-10T13:01:45+01:00
author: Gerco Dries
layout: page
guid: http://queuemanager.nl/offline-installation/
aktt_notify_twitter:
  - 'yes'
---
To perform an offline (non-webstart) installation of sonic message manager, you need to perform the following steps:

  1. Download [the main application jar file](http://queuemanager.nl/app/messagemanager-app-jar-with-dependencies-3.2.jar)
  2. Double click the downloaded jar file (eg. messagemanager-app-jar-with-dependencies-3.0.jar) to start message manager.
  3. Edit the appropriate profile and point the application to the client jar files for your SonicMQ, ActiveMQ or Solace installation.

Double clicking the jar file doesn&#8217;t work for everyone. If it doesn&#8217;t work for you, you can try to start the program with the following command line:

<pre>C:\&gt; java -jar messagemanager-app-jar-with-dependencies-3.2.jar</pre>

Please note that this mode of installation prevents you from receiving updates automatically. You may not be alerted if an update is available and will need to check the web site manually.