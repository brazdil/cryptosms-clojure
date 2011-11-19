CryptoSMS
=========

Introduction
------------

CryptoSMS is a project aiming to replace the original Messaging
application in Android, while providing the users with tools for
encrypting their text messages.

Project is developed mainly to explore the possibilities of combining
Clojure (functional programming language) with Java on the Android 
platform.

Dependencies
------------

You need the following to compile CryptoSMS yourself. Please follow
installation instructions on the projects' websites. 

 - [Android SDK](http://developer.android.com) 
   with Android 2.3.3 (API 9) SDK Platform
 - [Apache Ant](http://ant.apache.org/)
 - [Leiningen](https://github.com/technomancy/leiningen)

Compilation
-----------

Please note that these instructions assume that you have folders
containing the Android tools and `lein` in your $PATH. If not, you
will have to change the commands accordingly, or change your $PATH
environment variable.

Start by downloading the source code from GitHub:

    $ git clone git://github.com/davidbrazdil/cryptosms.git
    $ cd cryptosms

Create a configuration file with information about the location of
your Android SDK. If the command below doesn't work for you, try 
updating your Android SDK to the latest version.

    $ android update project --subprojects --path "."

Now simply let Ant do its job and compile the whole project (will
recursively compile the Clojure files using `lein`).

    $ ant debug

The command above will create file `bin/CryptoSMS-debug.apk`, which you
can now install on your phone either manually by copying it on the SD
card, or by running the following command.

    $ ant installd

Alternatively, you can compile the project and install the APK at the
same time by running:

    $ ant debug install

Credits
-------
 - [GreenDroid](https://github.com/cyrilmottier/GreenDroid) (ActionBar)
 - [Double-J Design](http://www.doublejdesign.co.uk/) (icons of lock)
