---
id: 240
title: Support for Solace messaging is now available in the nightly build
date: 2017-02-16T20:29:30+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=240
permalink: /2017/02/16/support-for-solace-messaging-is-now-available-in-the-nightly-build/
categories:
  - Uncategorized
---
The nightly build of Message Manager now has support for [Solace](http://www.solace.com) messaging. Please give it a try if you are interested. This is a very early version of the Solace support and it will have bugs, probably many.

Click here to run [MessageManager 3.1-SNAPSHOT](/v3/nightly/app/MessageManager.jnlp)

To be safe, make a backup of your configuration file for safety, you can find that in the following location depending on your operating system:

<table>
  <tr>
    <th>
      Operating System
    </th>
    
    <th>
      Location
    </th>
  </tr>
  
  <tr>
    <td>
      Windows
    </td>
    
    <td>
      %APPDATA%\MessageManager\config.xml
    </td>
  </tr>
  
  <tr>
    <td>
      Mac OS X
    </td>
    
    <td>
      ~/Library/Application Support/MessageManager/config.xml
    </td>
  </tr>
  
  <tr>
    <td>
      Linux
    </td>
    
    <td>
      ~/.config/MessageManager/config.xml
    </td>
  </tr>
</table>

If you have previously installed a nightly build, please refer to the above table and delete the &#8220;plugins/3.1-SNAPSHOT&#8221; directory in that same location before running this new build. The naming of the plugin jar files has changed and there may be conflicts if you have had previous versions installed.

When you find any bugs, please [report them](https://bitbucket.org/gerco/messagemanager/issues/new).