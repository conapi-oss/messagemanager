---
id: 226
title: First multi MQ test version available
date: 2016-02-05T22:53:09+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=226
permalink: /2016/02/05/first-multi-mq-test-version-available/
categories:
  - Uncategorized
---
I have just uploaded the first test build of the multi MQ version of Message Manager. This version supports both SonicMQ and ActiveMQ.

[Message Manager 3.0-SNAPSHOT](http://queuemanager.nl/v3/nightly/app/MessageManager.jnlp)

In order to use this application:

  1. Select the profile you want to use by clicking it in the list on the left
  2. Edit the profile by providing a name and set a classpath for your messaging system 
      * for Sonic MQ 7.0 or later select any jar (for example sonic_Client.jar) in the MQ/lib directory and the other required jars will be selected automatically
      * for ActiveMQ 5.11 or later select the activemq-all jar file in your ActiveMQ installation
  3. Click the &#8220;Activate Profile&#8221; button in the lower right corner of the window

This version is a separate download and will not overwrite any of the other versions you may have installed. Please provide any and all feedback on issues or improvements in the comments or by [filing an issue](https://bitbucket.org/gerco/messagemanager/issues/new) on Bitbucket.