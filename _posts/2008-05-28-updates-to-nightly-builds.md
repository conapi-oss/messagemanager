---
id: 14
title: Updates to nightly builds
date: 2008-05-28T11:17:22+02:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/2008/05/28/updates-to-nightly-builds/
permalink: /2008/05/28/updates-to-nightly-builds/
categories:
  - Uncategorized
---
The nightly build contains several important new features that need testing. Please focus your attention on the following:

  * Filtering of the queue list. The following wildcard characters are implemented: ? for a single character, \* for zero or more characters. Grouping is possible with (, | and ) like this: q.\*.(fault|rme).
  * Sending and saving of .esbmsg files. These files can contain a multi part message in one file, as well as store message headers and properties. They can be edited with an Xml editor and Sonic Workbench has a GUI editor for these files. You can save .esbmsg files by selecting the relevant filetype in the save dialog.

Please test the nightly build thoroughly and send me your findings. The sooner this gets tested properly, the sooner I can release version 1.4 to the general public.

As a side-note: Nightly sourcecode snapshots are now available on the [Download](/download) page.