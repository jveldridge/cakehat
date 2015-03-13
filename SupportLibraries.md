# Support Libraries #

cakehat relies upon a number of external jars. These libraries have proven themselves reliable.

## Joda-Time ##
http://joda-time.sourceforge.net/

`joda-time-2.1.jar`

Joda-Time is a library that provides date and time functionality that is far superior to the JDK's Calendar and Date classes.

## Javassist ##
http://www.csg.is.titech.ac.jp/~chiba/javassist/

`javassist-3.16.1.jar`

Javassist reads Java class files and allows for determining information about them without actually turning them into Java objects. (This is very different from Java reflection.) This functionality is used to determine the directory containing all of the root packages and which classes have main lines.

## Commons Compress ##
http://commons.apache.org/compress/

`commons-compress-1.4.1.jar`

Commons Compress can extract and create tar and zip files. cakehat uses this library to extract digital file handins.

## Guava ##
http://code.google.com/p/guava-libraries/

`guava-13.0.jar` {NOT LATEST}

Guava contains what was formally known as Google Collections. This library provides a large number of data structures not found in the standard Java library such as multimaps and immutable data structures. A very important detail is that most of these data structures do not support storing null.

## JavaMail ##
http://www.oracle.com/technetwork/java/javamail/index.html

`javamail-1.4.5.jar`

JavaMail allows for sending emails on behalf of the actual TA using cakehat.

## SQLite ##
http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC

`sqlite-jdbc-3.7.2.jar`

**This library uses native code**

SQLite is the database used by cakehat. SQLite is cross-platform C library, Xerial provides a Java wrapper.

## Java Native Access (JNA) ##
https://github.com/twall/jna

`jna-3.4.1.jar`

**This library uses native code**

JNA is used to access the POSIX functionality available on Linux that the Java standard libraries do not provide access to. This includes accessing group membership (for instance all users that belong to cs015student), the names of users, and changing file permissions.

## matlabcontrol ##
http://code.google.com/p/matlabcontrol/

`matlabcontrol-3.1.0.jar` {NOT LATEST}

matlabcontrol allows cakehat to programmatically control a running instance of MATLAB.