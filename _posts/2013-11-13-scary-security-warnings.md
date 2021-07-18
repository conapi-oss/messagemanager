---
id: 157
title: Scary Security Warnings
date: 2013-11-13T23:05:54+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=157
permalink: /2013/11/13/scary-security-warnings/
categories:
  - Uncategorized
---
Since Java 7 update 40 (or so), users have been getting scary security warnings with red text of the following form:

[<img loading="lazy" class="alignnone size-medium wp-image-158" alt="security warning" src="http://queuemanager.nl/wp/wp-content/uploads/2013/11/security-warning-300x207.png" width="300" height="207" srcset="https://queuemanager.nl/wp/wp-content/uploads/2013/11/security-warning-300x207.png 300w, https://queuemanager.nl/wp/wp-content/uploads/2013/11/security-warning.png 524w" sizes="(max-width: 300px) 100vw, 300px" />](http://queuemanager.nl/wp/wp-content/uploads/2013/11/security-warning.png)

This is part of a new initiative by Oracle to increase the security of Java in the browser and cannot be disabled or dismissed (they removed the &#8220;don&#8217;t ask again&#8221; option). Starting with the January 2014 Java update, it will no longer be possible to run code that is not signed with a certificate issued by a recognized certificate authority.

Sonic Message Manager is free and I would like to keep it free. Since these certificates cost several hundred US$/year, I will probably change the distribution model away from Java Web Start and towards a more traditional &#8220;installer&#8221; based model. Suggestions on how to deal with this are welcome in the comments or by email.

&nbsp;