Media MicroCloud, Encrypted Photo and File Micro-Cloud App
====================================================

Media MicroCloud (MM) is a highly innovative security app for your Android phone or tablet. 
It combines the CamCipher camera, the IOCipher AES encrypted file system and a Nanohttpd 
web server.  You can record, save and manage encrypted photos and videos. The encrypted 
file system can not only protect jpg and mp4 files, but any files you want to keep safe 
and secure. This includes PDF files, PowerPoint, MS Word documents and dozens of other 
file types. Do you have important files you want to keep encrypted in your pocket?  
This app is for you.

The popular elFinder and PhotoSlide apps make access to all your files easy.  A secure 
MicroCloud web server provides access your files to every browser on your WiFi LAN to 
authenticated users. This means you can keep an encrypted volume of information on the 
Android in your pocket, and have access to it from all the computers, tablets, and TVs 
on work or home network. If you have hotspot capability it works with any device that 
can connect to your hotspot.

App Objectives
------------------
The #1 objective is to maintain privacy for your photos and files.
Based on a survey of 100 users by the author, on average 69 apps can access
your photos and files on Android. Combine photo file access and Internet access
and your privacy is at risk.

Media MicroCloud creates an encrypted `Crypto` volume.  The native photo app saves photos
directly to the Crypto volume. The photo gallery provides a rich environment to 
view your Crypto photos and if you so choose, share your Crypto photos.

An additional objective is to protect files.  The Crypto volume is an excellent place to keep 
legal documents such as PDF files, MS Word or PowerPoint files, or simple text files.
The Crypto volume is carried in your pocket for use on the go. When you return to your 
home or work WiFi LAN, use a micro-cloud bookmark to access all your files on using
the big screen on your PC or Mac.

Installation
------------
1. Connect to a WiFi access point or start your Android hotspot.
2. Download Media MicroCloud from Google Play or directly from nuvolect.com/mediamicrocloud.
3. Start the app, agree to terms and conditions and on the home screen note
the IP address used by the MicroCloud server.
4. Select Camera, note '--CRYPTO --' in the menu bar, and take a test photo.
5. Enter the micro-cloud IP address in any browser that is on the WiFI network, bookmark it.
6. Select `Crypto Gallery` to see your new photo.

To Secure Your Crypto Volume
----------------------------
Initially the app is wide open. To secure the app you need to define username and password
for the web app and also enable the entry lock.
1. Select Settings-Web user credentials and enter a username and password.
2. Select Settings-Media MicroCloud entry lock and either register your YubiKey 
device or set a good enty passphrase.

Basic Usage
-----------
It is important to understand the utility of the Android back-button. 
From the Home screen, the back-button exits and locks the app.
If you don't want to continually lock and ulock the app, exit the app
by migrating to another app.  It will remain ulocked until either the 
device is restarted or exited with the back-button.

Files can be moved to the Crypto volume using the File Manager app.
Simply select `copy` and then `paste`.
You can run the File Manager either from your Android device or the
large screen on your PC or Mac.
Certain operations may be a little slower on the Crypt volume do to the 
computing required for encryption.

If you frequently use a hotspot in addition to a WiFi LAN, note and 
bookmark the micro-cloud IP address. If you use different WiFi environments
the IP address for the micro-cloud will likely be different for each one.

Note that some WiFi routers will block certain server prototcols.  This is 
is to protect users from man-in-the-middle attacks.

About Security
--------------
Media MicroCloud is a micro-cloud application that only allows access to devices on your
WiFi LAN. Data is stored in an `IOCipher` AES 256 bit encrypted volume.
Network communications are protected with HTTPS TLS 1.2 encryption.
Google Analytics is used for software quality purposes, otherwise no 
communications are made with the Internet.

The micro-cloud server is hosted on your Android device and does not use
a domain name or static IP address as a traditional web server will.
Consequently, a self-signed secruity certificate is used for HTTPS encryption.
Upon inspection, this certificate can be saved into your computers keychain to
avoid the "THIS SERVER IS NOT SAFE" warning. Note that a self-signed certificate 
still provides high-grade AES encryption but the certificate cannot be verified as attached
to a domain name or static IP address, hence the warning.

Tested Devices
--------------
The [compatibility status page](https://nuvolect/blog/mm_supported_devices) outlines
what devices have been tested with this app.

Additional Documentation
------------------------

License
-------

[End-User License Agreement](https://nuvolect/blog/mediamicrocloud_terms) 

Questions
---------
For problems or feature suggestions, use the "Issues" link on this page.

For offline discussion email the developer at team@nuvolect.com or for secure
email use nuvolect@protonmail.com.

**Be certain to include complete steps for reproducing the issue.**

Do not ask for help via social media.

Who Made This?
--------------
<a href="http://nuvolect.com">![Nuvolect LLC](https://nuvolect.com/img/nuvolect_logo_name_low_59x180.png)</a>

