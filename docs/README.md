# DeepDive-Android
An educational tool for the reverse engineering and security assessment of Android apps

We initially experimented with reverse engineering to explore vulnerabilities of our own apps. 
We learned how hackers find security gaps in our apps. Looking other apps on our phone, 
we also learned how seemingly legitimate apps invade our privacy.

## Finally, an easy workflow you can live with
Android is the server. Access the details of reverse engineering from your handheld device 
or from the convenience of your large Mac or PC screen. How we use Deep Dive:
1. Join the same WiFi on your Android phone or tablet as your Mac or PC.
1. Install and start Deep Dive on your Android phone or tablet.
1. Note the IP address on the home screen, go to that address from your Mac or PC. 
1. See Notes On Security.

## Notes On Security
All communications are encrypted with a self signed security certificate. 
All communications are AES encrypted via TLS 1.2 HTTPS however your browser is unable to 
associate the certificate with a domain name or static IP address. 
You will see a security warning or error message. 

## Lucene — Ultra-fast search
A decompiled app can have hundreds of thousands of files and millions of lines of code.
That's why DeepDive has Lucene, a near-instant search capability, the search engine behind Amazon, 
AOL, LinkedIn, and Twitter. You can index the source code from a single app or index the entire 
virtual filesystem and search across hundreds of apps.

#### Search for hundreds of keywords at a time
Search Sets are collections of Lucene Searches. Search for up to a 100 keywords at a time. 
The app comes with default Search Sets and you can create your own.

## Features
* The source code is at your fingertips using the elFinder "Mac-like" file manager.
* Your code is secure on your hardware, not on an unknown server in the Cloud.
* Additional tools include Keystore, Shell, and Logcat.
* APK extraction and unpacking
* DEX optimization
* Convert Android DEX to Jar
* Decompile most Android apps
* Private and crypto file storage
* Instant search and search sets
* Shell, Logcat & Keystore utilities
* Android API 24+, Nougat
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

# We Use Analytics
The purpose is for engineering and user experience. 
Analytics provide anonymous eyes into use of the application that can help with 
unserstanding user intent and how to improve the application.

## Licensing
* This version is free via GPLv3, enjoy! Please provide feedback.
## Contributing Bug Reports
We use GitHub for bug tracking. Please search the existing issues for your bug and create 
a new one if the issue is not yet tracked.

## Support
For troubleshooting and support information, please visit the wiki.
<https://github.com/teamnuvolect/DeepDive-Android/wiki>

For general questions and discussion about features, visit the forum.
<a href="https://nuvolect.freeforums.net/board/4/discussion-deepdive">

Copyright Nuvolect LLC

