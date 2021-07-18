---
id: 89
title: Sonic Message Manager 2.1 released
date: 2009-09-28T11:46:31+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=89
permalink: /2009/09/28/sonic-message-manager-2-1-released/
categories:
  - Uncategorized
---
Version 2.1 of sonic message manager has just been released and it fixes the following bugs and adds a few small extra features:

<ul style="margin-top: 5px; margin-right: 0px; margin-bottom: 0px; margin-left: 10px; padding: 0px;">
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/34">34: Bytes message viewer only works right the first time for some messages</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/47">47: Redesign JMS properties</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/131">131: Handling default connections</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/137">137: Add option for persistent/non persistent message sending</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/141">141: Allow connections to brokers behind NAT routers</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/142">142: Refresh of deleted messages doesn&#8217;t work</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/143">143: Separate task queues for multiple brokers</a>
  </li>
  <li style="list-style-type: none; list-style-image: none; margin-bottom: 0px; color: #777777; margin-top: 3px; margin-right: 0px; margin-left: 0px; padding: 0px;">
    <a style="color: #0066cc; text-decoration: none;" href="http://queuemanager.nl/trac/ticket/145">145: Sort queues by number of messages</a>
  </li>
</ul>

Depending on your version of Java Web Start, you may or may not get a message requesting you to upgrade. Some versions will download the new version immediately and others will do so in the background and run the new version the next time the application is started. If you have not received the new version after a system reboot, you may need to remove and re-install the application.

This version of Sonic Message Manager is released under the Apache License v2.0, as will all future versions. This license means more freedom to developers wanting to reuse some of the code and allows for more interoperation with other programs and libraries. It also solves the legal issues there used to be with releasing under the GNU GPL. This wasn&#8217;t the GPLs fault, it was mine.