---
id: 203
title: Nightly Builds Signed With New Certificate
date: 2014-12-14T17:09:35+01:00
author: Gerco Dries
layout: post
guid: http://queuemanager.nl/?p=203
permalink: /2014/12/14/nightly-builds-signed-with-new-certificate/
categories:
  - Uncategorized
---
I&#8217;ve obtained a new code signing certificate and used it to sign the nightly builds. Since this is a new certificate, you may see error messages if Java Web Start was unable to update all downloaded files at once. If you get a message stating that &#8220;Not all files are signed with the same certificate&#8221; or &#8220;The signed version does not match the downloaded version&#8221;, clear your Java Web Start cache and try again.

In rare cases, Java Web Start may get stuck and there is no way to fix it. In that case, you will have to resort the stable builds or offline installation for 24h to give Java Web Start some time to realize that it needs to update the rest of the files.