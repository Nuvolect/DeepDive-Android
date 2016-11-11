# Sprint Current Release  
* Integrate Lucene
** Tune search to work with java source files, exact search match
** Update ? help with working examples

* Expose Intent Filters, https://github.com/jaredrummler/APKParser

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
FernFlower
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

