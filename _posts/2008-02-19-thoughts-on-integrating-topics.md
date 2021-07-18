---
id: 6
title: Thoughts on integrating topics
date: 2008-02-19T21:04:53+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/2008/02/19/thoughts-on-integrating-topics/
permalink: /2008/02/19/thoughts-on-integrating-topics/
categories:
  - Uncategorized
---
There are several ways to implement topic support in a Sonic MQ client, like the message manager. The most obvious way would be to implement one or two extra tabs for receiving from and sending to topics.

Another approach would be to integrate topic support into the existing view- and send tabs and leverage the, already familiar, functionality in those tabs.

The question remains as to what would be a sensible user interface for either scenario. How would the user indicate a topic to send or listen to? A topic list is not available and adding more buttons or even a pop up menu is quite a horrible solution in my opinion.

I&#8217;d like to hear your thoughts on this. Feel free to comment and post screenshots or mockups.