# Sprint- minimal Tasks for pilot study
* search path has double //
* Current index conflict
* Fix search result Angular errors

* Header design, developer menu

* Include common header.htm 
* Include common footer.htm

** Update ? help with working examples
* Clear upload package name from dialog
 * Provide pop-up/user feedback folder created

# Backlog
 
* apps.htm, show "No current working apps" when there are none
* Indexing... eliminate multiple lines Indexing: /DeepDive/[package name]
* Test refactoring of file links and preview
* Add Probe and ProbeMgr classes, convert Decompile.java to be an instance of Probe
* Background thread creation of bitmaps
* Document multi-browser decompilier conflicts
* Is there a better way to organize processess to monitor progress and kill-all

* Integrate TinyMCE
https://github.com/Studio-42/elFinder/wiki/Using-custom-editor-to-edit-files-within-elfinder

# Performance timing server and Android DD Apps
com.amazon.kindle, large apk at 52.41 MB
o Dex 2 JAR, Android 4 min 19 seconds
o Dex 2 JAR, ddServe 0 min 27 seconds
o CFR decompile, Android 30 min 24 seconds
o CFR decompile, ddServe 2 min 14 seconds
com.evernote, 33.701,878 bytes
o Dex 2 JAR, Android 3 min 32 seconds
o Dex 2 JAR, ddServe 0 min 17 seconds
o CFR decompile, ddServe 2 min 21 seconds


# Extract a backup from an app
adb backup com.packagename.android
java -jar abe.jar unpack backup.ab
tar -xvf backup.tar

# Extract the public and private keys used by an app
Android Keystore public and private keys are stored in the /data/misc/keystore/user_0 directory.  
The private key is stored in a file that has <app_id>_USRCERT_<key_alias>.  
On a rooted phone you can copy the file to another <app_id_malicious>_USRCERT_<key_alias> 
and then import it from your malicious app, allowing you to recover the password.

* Decompile DEX
* Fix problem killing process
* Decompile process status
** Schedule polling when a task is active and responding
** Disable "running" STOP when task is not active 

** Uncaught exception: java.lang.OutOfMemoryError: Failed to allocate a 8204 byte allocation with 3227784 free bytes and 3MB until OOM; failed due to fragmentation (required continguous free 12288 bytes where largest contiguous free 8192 bytes)
** Uncaught exception: PenTest Thread Group

# Explore
* ClassyShark https://github.com/google/android-classyshark

# Sprint NEXT Release  
* Add app icon to Apps, App and Decompile pages
* Add user search

# Backlog Tasks  
* Check critical file & folder permissions
* Browser logout when non-activity period expires
* Read call log: http://android2011dev.blogspot.com/2011/08/get-android-phone-call-historylog.html
* Read call log: https://codexplo.wordpress.com/2014/04/17/how-to-read-call-logs-programmatically-from-android/ 

ANGULAR NOTES:
http://angular-filemanager.zendelsolutions.com/
The JS part does not initialize properly.
Source code does not include any backend support.  This will need to be written.

show-java
https://github.com/niranjan94/show-java

DECOMPILER NOTES:
CFR:
http://www.benf.org/other/cfr/
Fernflower
https://github.com/fesh0r/fernflower
APK Parser
https://github.com/caoqianli/apk-parser
http://www.java2s.com/Code/Jar/a/Downloadandroidsunjarsignsupport11jar.htm

Lucene Then & Now: https://www.youtube.com/watch?v=5444z-L2V2A

Android java.nio.file code
https://github.com/pwnhack/android/tree/master/dex2jar/d2j-j6/src/main/java/pxb/java/nio/file

# Using Java 1.8 lambda on Java 1.7
https://github.com/evant/gradle-retrolambda

# When updating elFinder
* left panel width, elFinder.full.css, line 1173, 	width:150px;/* mkk 230px */
* add full screen support
		<!-- mkk start, added for full screen-->
		<style type="text/css">
			html, body {
			height: 100%;
			margin: 0;
			}

			#elfinder {
			min-height: 100%;
			}
		</style>
		<!--[if lte IE 6]>
		<style type="text/css">
			#container {
			height: 100%;
			}
		</style>
		<![endif]-->
		<!-- mkk end, added for full screen-->
		

ELFINDER JAVA CONNECTOR NOTES:
https://github.com/bluejoe2008/elfinder-2.x-servlet

