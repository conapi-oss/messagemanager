---
id: 25
title: Sonic Message Manager 1.6.2
date: 2009-01-05T14:02:37+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/2009/01/05/sonic-message-manager-162/
permalink: /2009/01/05/sonic-message-manager-162/
categories:
  - Uncategorized
---
Sonic Message Manager 1.6.2 has been released a few moments ago. This version contains the following fixes and updates:

  * (#102) SMM now only allows a user to &#8220;Clear messages&#8221; when they can also read from the specified queue. Send permissions are not required because someone who can read from the queue could also just consume all messages if they wanted to remove them.
  * (#103) In some installations of Sonic, the acceptors on the broker are configured using &#8220;localhost&#8221; as the hostname. SMM will now attempt to resolve the actual hostname of the broker host before connecting to it.
  * (#104) Cancel an in-progress queue browsing when &#8220;Clear messages&#8221; is clicked to prevent having to wait for the browsing to complete before deleting the messages.