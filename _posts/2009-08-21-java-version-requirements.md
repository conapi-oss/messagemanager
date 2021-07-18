---
id: 74
title: Java version requirements
date: 2009-08-21T21:04:54+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=74
permalink: /2009/08/21/java-version-requirements/
categories:
  - Uncategorized
---
I have received some reports of trouble running the Sonic 6.1 version of Sonic Message Manager on Java 1.6.0_15. Because of this, I have changed the platform requirements for that specific version of SMM to Java 1.5.x &#8211; 1.6.14.

If you only have Java 1.6.0_15 or higher installed, java web start will attempt to install a compatible JRE for you if you want to run the Sonic 6.1 version of SMM. If you are using any other version of Sonic, any Java 1.5 or higher JVM will work just fine.

This is a temporary measure until I find out what Sun has changed in j2se 1.6.0_15 and what I need to do to work around it (if at all possible).