---
id: 54
title: Please test the nightly build
date: 2009-04-07T14:48:31+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=54
permalink: /2009/04/07/new-features-in-the-nightly-build/
categories:
  - Uncategorized
---
I have just merged a bunch of pending changes from the 2.0 branch into the nightly build in order to try and fix some bugs regarding dragging messages with odd JMSDestination values. This means that the nightly build may be somewhat more unstable than usual.

I would like to ask for your help and for you to use the nightly build as much as possible the coming few weeks to give it a thorough test.

The most interesting change is the possibility to connect to different brokers from the browse- and send tabs at the same time. Most other changes are internal and mostly regard preparations for opening listener windows on queues (and possibly topics).