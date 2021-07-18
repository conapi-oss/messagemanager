---
id: 212
title: First Message Manager 3.0 Beta is available
date: 2015-02-14T22:34:51+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=212
permalink: /2015/02/14/first-message-manager-3-0-beta-is-available/
categories:
  - Uncategorized
---
I&#8217;ve been working on Message Manager 3.0, which will replace the 2.x branch Sonic Message Manager in time. This will be a separate download and will not automatically upgrade from 2.x versions for technical reasons. There are major changes in the application, but most of those are invisible to the users:

  * Internal refactoring that allows for a plugin system in the near future. It will be possible to create and install plugins for custom message formats, other messaging systems, new UI tabs and more.
  * Syntax highlighting XML editor/viewer
  * Faster download due to Pack200 compression, it&#8217;s now less than 3MB to download.
  * Java 6 is now required
  * Support for Sonic 6.1 was dropped
  * Support for Sonic 2015 was added

The first beta version of 3.0 is available for download here. These versions will automatically update to the latest code on a nightly basis and should be considered unstable.

  * [Message Manager for Sonic MQ 7.0 NIGHTLY](/v3/nightly/7.0/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 7.5 NIGHTLY](/v3/nightly/7.5/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 7.6 NIGHTLY](/v3/nightly/7.6/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 8.0 NIGHTLY](/v3/nightly/8.0/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 8.5 NIGHTLY](/v3/nightly/8.5/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 2013 NIGHTLY](/v3/nightly/8.6/SonicMessageManager.jnlp)
  * [Message Manager for Sonic MQ 2015 NIGHTLY](/v3/nightly/10.0/SonicMessageManager.jnlp)

The [source code](https://bitbucket.org/gerco/messagemanager) for this version is available on Bitbucket, please [report any issues here](https://bitbucket.org/gerco/messagemanager/issues/new).

Future versions of Message Manager 3.0 will no longer be specific to a particular version of Sonic MQ. At that time, you will have to download a new version of the application.