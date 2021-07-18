---
id: 162
title: Nightly builds are now officially signed
date: 2013-12-01T06:19:54+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=162
permalink: /2013/12/01/nightly-builds-are-now-officially-signed/
categories:
  - Uncategorized
---
In light of the coming tightening up of Java security by Oracle (see my previous post), I have purchased a code signing certificate and started signing the nightly builds. To check for any compatibility issues, please try them out and see whether there are any problems running them.

If you find anything, let me know by email (my address is in the &#8220;Help&#8221; tab, under &#8220;Developer contact information&#8221;).

Please note that on Mac OS X 10.9 (Mavericks), the application will still appear to be unsigned (&#8220;from an unidentfied developer&#8221;) and the built-in security mechanism &#8220;Gatekeeper&#8221; will refuse to open the application. The reason for this is that Gatekeeper only accepts code signing certificates issued by Apple. You can get around this by right-clicking on the jnlp file and selecting &#8220;Open&#8221;. The dialog that pops up will have the option of running the application.

In hopes of recouping some of the cost for purchasing a code signing certificate, I have also added a Paypal Donate button to the website and the initial tab of the application. Please donate generously to support further developments!