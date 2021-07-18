---
id: 71
title: Update mechanism changed
date: 2009-07-25T23:10:10+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=71
permalink: /2009/07/25/update-mechanism-changed/
categories:
  - Uncategorized
---
If you are running Sonic Message Manager with Java version 1.6.0_10 or later, the update policy for Sonic Message Manager has been changed. 

Java Web Start will now check for updates in the background, while starting the application. When an update has been found, you will be informed of this the **next** time you start the application and you will have the option to install it.

This improves startup times because the update check is no longer done before starting the app. It should also dramatically improve startup time when you are not connected to the internet.