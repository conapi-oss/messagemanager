---
id: 252
title: Message Manager 3.1 released
date: 2018-02-13T16:12:15+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=252
permalink: /2018/02/13/message-manager-3-1-released/
categories:
  - Uncategorized
---
It&#8217;s been a while since I released a new version, but here is version 3.1 of Message Manager. It contains the following changes:

  * Support for [Solace](https://www.solace.com) messaging
  * There is now a search box that allows you search through message headers, properties, message body and all parts of a SonicMQ multi-part message. (#35)
  * There is a Swing Look and Feel selector in the settings tab (requires restart)
  * Fix display of large message sizes in queues table (#22)
  * Added JMSPriority field to message sender

If you are already running version 3.0, your application will automatically upgrade. If not, click [run Message Manager 3.1](https://queuemanager.nl/app/MessageManager.jnlp) to download and run the new version.