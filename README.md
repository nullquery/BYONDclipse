BYONDclipse
===========
Introduction
------------
BYONDclipse is a set of plugins and features to allow people to develop using 
the BYOND games development platform, in Eclipse.

How to develop BYONDclipse
---------------------------
For building BYONDclipse, you require as minimum:

 * Maven 3.3.x
 * JDK 1.8
 * A mirror in your .m2/settings.xml containing the URL
    http://www.awesomeware.org/nexus/content/groups/public

Once all set up and assuming mvn is in your PATH, the following can be used to 
package a local P2 repository you can use in testing:

> mvn clean package

The maven project contained in this repository can also be imported for 
development in Eclipse via m2e, using Import > Maven > Existing Maven Project.