---
id: 58
title: Topic support is coming!
date: 2009-06-26T13:06:02+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=58
permalink: /2009/06/26/topic-support-is-coming/
categories:
  - Uncategorized
---
In the new nightly build (available now), there is some alpha-level topic support. In order to include this, a lot needed to change. Therefore I urge everyone to give the nightly a good shakedown. 

Special thanks go to Hai Nguyen from [Flusso](http://www.flusso.nl) for writing the initial version of this code! Not all of it has been properly integrated yet, I&#8217;m still working on that.

Things that are known to be missing:

  * No way to delete messages from a topic buffer so OOM is unavoidable over time
  * Topics added to the topic subscriber table are not yet saved
  * There is no way to send a message to a topic

These issues are known and will be addressed as time permits. When you find an issue not on this list, please report it on trac. You will need to [create a wordpress account](http://queuemanager.nl/wp/wp-login.php?action=register) first and then use that to log into trac.

You can also e-mail the bugs to **gdr at progaia dash rs dot nl**.