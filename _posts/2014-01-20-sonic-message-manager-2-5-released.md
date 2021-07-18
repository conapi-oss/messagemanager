---
id: 176
title: Sonic Message Manager 2.5 released
date: 2014-01-20T05:53:45+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=176
permalink: /2014/01/20/sonic-message-manager-2-5-released/
categories:
  - Uncategorized
---
Sonic Message Manager 2.5 has just been released. The major changes are:

  * Support for Sonic 2013 (8.6) added
  * Code signing with proper CA certificate instead of self-signed certificates
  * Added size column in the queue table for version of Sonic that support it (7.0 or later)
  * Added donate button on the connection tab

I noticed that some users are having trouble with auto-updating because the code signing certificate has changed. It may be neccesary to remove the application using the Java Web Start control panel (run javaws -viewer from the command line) and then reinstall it. Simply downloading a new jnlp file from the website may not be sufficient to get Java Web Start to update all previously downloaded jar files.