---
id: 78
title: Sonic Message Manager is now Sonic-independent
date: 2009-08-31T10:54:00+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=78
permalink: /2009/08/31/sonic-message-manager-is-now-sonic-independent/
categories:
  - Uncategorized
---
(In theory, at least)

The nightly build of SMM now has all Sonic-specific logic separated from the main program. The main program compiles without any references to SonicMQ libraries and this opens the door to implementing support for other MQs.

All sonic-specific code is to be found in the &#8220;sonic-src&#8221; directory of the mercurial repository, all core code is in &#8220;src&#8221;. There probably still are some Sonic-specific assumptions in there, we&#8217;ll have to work those out when we get to them. 

If anyone is interested in implementing support for another MQ, please contact me before writing any code so I can explain the process and structure of the application to you. I will document this in time, but not right now.