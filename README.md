# JBoss EAP(v6.4) gradle plugin

![build-status](https://travis-ci.org/newnewcoder/jboss-curl-gradle-plugin.svg?branch=master)

Gradle plugin that deploys application (single war) to JBoss EAP server.

Using ~~curl to call~~ HTTP management API from JBoss EAP server. ~~So you need to install `curl`.~~

This plugin provides following tasks:

    1. add: 
       upload war to JBoss EAP server and add it as application.
    
    2. remove: 
       remove application from JBoss EAP server.
    
    3. deploy: 
       add war to JBoss EAP server and enable application.
    
    4. enable: 
       enable application from JBoss EAP server.
    
    5. disable: 
       disable application from JBoss EAP server.
    
    6. status: 
       print the JBoss EAP server status

## How to use
In `build.gradle`

First, setting `buildscript` block as below:
~~~groovy
buildscript {
    repositories {
        maven {url "https://plugins.gradle.org/m2/"}
    }
    dependencies {
        classpath 'gradle.plugin.com.github.newnewcoder:jboss-curl-gradle-plugin:1.0.1-SNAPSHOT'
    }
}
apply plugin: 'com.github.newnewcoder.jbosseap64-simple-deploy'
~~~

Fill in `jboss` block with JBoss console connection info:
~~~groovy
jboss {
    host = '<JBoss AP Server host>'
    port = '9990' //default 9990
    user = '<user>'
    password = '<password>'
    warName = '<war file name that will be deploy>'
    warPath = '<war file path>'
    debug = false //set to true for debug mode, that just print Request object's properties.
}
~~~

Now you can print all tasks and try deploy!

~~~sh
gradlew tasks --all
~~~