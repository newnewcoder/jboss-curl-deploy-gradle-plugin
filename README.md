# JBoss EAP(v6.4) gradle plugin

![travis-ci](https://travis-ci.org/newnewcoder/jboss-curl-gradle-plugin.svg?branch=master)
[![license](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![download](https://api.bintray.com/packages/newnewcoder/generic/jboss-curl-deploy-gradle-plugin/images/download.svg) ](https://bintray.com/newnewcoder/generic/jboss-curl-deploy-gradle-plugin/_latestVersion)

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
    repositories{
        jcenter()
    }
    dependencies {
        classpath 'com.github.newnewcoder:jboss-curl-deploy-gradle-plugin:1.0.0'
    }
}
apply plugin: 'com.github.newnewcoder.jboss-curl-deploy-gradle-plugin'
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