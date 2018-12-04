# DeepDive-Android
An educational tool for the reverse engineering and security assessment of Android apps

We initially experimented with reverse engineering to explore vulnerabilities of our own apps. We learned how hackers find security gaps in our apps. Looking other apps on our phone, we also learned how seemingly legitimate apps invade our privacy.

This is an early release, DeepDive is still in BETA. We need <a href="https://nuvolect.com/donate">your donations</a> to help support research, development and testing.

## Finally, an easy workflow you can live with
Android is the server. Access the details of reverse engineering from your handheld device or from the convenience of your large Mac or PC screen. How we use DeepDive:
1. Join the same WiFi on your Android phone or tablet as your Mac or PC.
1. Install and start DeepDive on your Android phone or tablet.
1. Note the IP address on the home screen and go to that address from your Mac or PC.
<br>
<a href="https://nuvolect.com/deepdive/deepdive-release.apk">
<img src="https://nuvolect.com/img/dd_download_icon.png" width="214" height="64"></a>
<br>
<a href="https://nuvolect.com/deepdive/install.htm">Installation Instructions</a>

## Notes On Security
Configure a user login from app Settings. All communications are encrypted with a self signed security certificate. All communications are AES encrypted via TLS 1.2 HTTPS however your browser is unable to associate the certificate with a domain name or static IP address,  consequently you will see a security warning or error message.

## Lucene — Ultra-fast search
A decompiled app can have hundreds of thousands of files and millions of lines of code.
That's why DeepDive has Lucene, a near-instant search capability, the search engine behind Amazon, AOL, LinkedIn, and Twitter.
You can index the source code from a single app or index the entire virtual filesystem and search across hundreds of apps.

#### Search for hundreds of keywords at a time
Search Sets are collections of Lucene Searches. Search for up to a 100 keywords at a time. The app comes with default Search Sets and you can create your own.

## Features
* The source code is at your fingertips using the elFinder "Mac-like" file manager.
* Your code is secure on your hardware, not on an unknown server in the Cloud.
* Device details, app details and permissions
* APK extraction and unpacking
* DEX optimization
* Convert Android DEX to Jar
* Decompile most Android apps
* Private and crypto file storage
* File manager and finder
* Instant search and search sets
* Shell, Logcat & Keystore utilities
* Android API 24+ (Nougat+)
* Root not required

## Tools included
* <a href="https://github.com/jaredrummler/APKParser">APK parser by Jared Rummler</a>
* <a href="https://github.com/JesusFreke/smali/wiki">DEX disassembler by Jeasus Freke</a>
* <a href="https://github.com/pxb1988/dex2jar">DEX2JAR by Bob Pan</a>
* <a href="http://www.benf.org/other/cfr/">CFR by Lee Benfield</a>
* <a href="https://github.com/skylot/jadx">Jadx by Skylot</a>
* <a href="https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine">Fernflower by JetBrains</a>
* <a href="https://lucene.apache.org/">Lucene search engine</a>
* <a href="https://github.com/NanoHttpd/nanohttpd">Nanohttpd web server</a>
* <a href="https://github.com/Studio-42/elFinder">elFinder file manager</a>
* <a href="https://lucene.apache.org/">Apache Lucene</a>

## We Use Analytics
The purpose is for engineering and user experience. 
Analytics provide anonymous eyes into use of the application that can help with 
unserstanding user intent and how to improve the application.

## Licensing
* DeepDive is offered for free under conditions of the <a href="https://www.gnu.org/licenses/gpl-3.0.en.html">GPLv3</a> open source software license.
* Contact <a href="https://nuvolect.com/contact.htm" >Nuvolect LLC</a> for a less restrictive commercial license if you would like to 
use the software without the <a href="https://www.gnu.org/licenses/gpl-3.0.en.html">GPLv3</a> restrictions.

## Contributing Bug Reports
We use GitHub for bug tracking. Please search the existing issues for your bug and create a new one if the issue is not yet tracked.
<https://github.com/Nuvolect/DeepDive-Android/issues>

## Support
* For troubleshooting and support information, please visit the wiki. <https://github.com/teamnuvolect/DeepDive-Android/wiki>
* <a href="https://nuvolect.com/donate">Consider making a donation to help fund support and development efforts.</a>

For general questions and discussion about features, visit the forum.
<a href="https://nuvolect.freeforums.net/board/4/discussion-deepdive">
<img src="https://securesuite.org/img/forum_join_chat.png"  height="50" width="134"></a>

Copyright Nuvolect LLC

