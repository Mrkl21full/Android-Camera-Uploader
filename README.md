Android Camera Uploader
==============

* Copyright (c), 2018, Krystian Kl

REQUIREMENTS
------------

- PHP 5 or latest
- Android Studio 3.2 or older (for compiling apk)

#### Suggested extensions for PHP:
- json encoder & decoder: Required for android app
- mysql (or mysqli for php 7 or latest): Its optional but if you want to integrate it with your system you'll need this

CURRENT TESTED PLATFORMS
------------------------

- Android 8.1 Oreo
- Android 7.1.1 Nougat
- Android API > 25 

KNOWN PROBLEMS
--------------

- Image weight to 4 or 8MB (Solution: Try to increase post size in PHP settings [upload_max_filesize, post_max_size])
- App crash after selecting 'Take picture' or 'Record video' (Solution: Try to upgrade your Android OS. This App wasn't created for Android API < 24)

SENSOR RELATED INFORMATION
---------------------------

When application is uploading image or video to the server, the ability to rotate the phone is blocked.

LICENSING
---------

This application is released under the GNU Public License Version 3 or 
(at your option) any later version, see [COPYING](COPYING) for details.

OTHER USEFUL INFORMATION
---------

This application is based on the Ravi's procject from Android Hive and Abhishek's project from Android Deft.
