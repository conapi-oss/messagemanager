---
id: 234
title: Multi-MQ Message Manager 3.0 is now available
date: 2016-06-24T03:35:48+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=234
permalink: /2016/06/24/multi-mq-message-manager-3-0-is-now-available/
categories:
  - Uncategorized
---
The multi MQ version of Message Manager 3.0 is now available, this version supports both SonicMQ and ActiveMQ.

Click to run [Message Manager 3.0](http://queuemanager.nl/app/MessageManager.jnlp)

In order to use this application:

  1. Select the profile you want to use by clicking it in the list on the left
  2. Edit the profile by providing a name and set a classpath for your messaging system 
      * for Sonic MQ 7.0 or later select any jar (for example sonic_Client.jar) in the MQ/lib directory and the other required jars will be selected automatically
      * for ActiveMQ 5.11 or later select the activemq-all jar file in your ActiveMQ installation
  3. Click the &#8220;Activate Profile&#8221; button in the lower right corner of the window

This version is a separate download and will not overwrite any of the other versions you may have installed. Please provide any and all feedback on issues or improvements in the comments or by [filing an issue](https://bitbucket.org/gerco/messagemanager/issues/new) on Bitbucket.