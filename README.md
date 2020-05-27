<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2018-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->

# Nexus Repository Puppet Format

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sonatype.nexus.plugins/nexus-repository-puppet/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.sonatype.nexus.plugins/nexus-repository-puppet)

[![CircleCI](https://circleci.com/gh/sonatype-nexus-community/nexus-repository-puppet.svg?style=shield)](https://circleci.com/gh/sonatype-nexus-community/nexus-repository-puppet) [![Join the chat at https://gitter.im/sonatype/nexus-developers](https://badges.gitter.im/sonatype/nexus-developers.svg)](https://gitter.im/sonatype/nexus-developers?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![DepShield Badge](https://depshield.sonatype.org/badges/sonatype-nexus-community/nexus-repository-puppet/depshield.svg)](https://depshield.github.io)

## Developing

### Requirements

* [Apache Maven 3.3.3+](https://maven.apache.org/install.html)
* [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Network access to https://repository.sonatype.org/content/groups/sonatype-public-grid

Also, there is a good amount of information available at [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development)

### Building

To build the project and generate the bundle use Maven

    ./mvnw clean package

If everything checks out, the bundle for puppet should be available in the `target` folder

#### Build with Docker

    docker build -t nexus-repository-puppet .

#### Run as a Docker container

    docker run -d -p 8081:8081 --name nexus-repository-puppet nexus-repository-puppet

For further information like how to persist volumes check out [the GitHub repo for our official image](https://github.com/sonatype/docker-nexus3).

After allowing some time to spin up, the application will be available from your browser at http://localhost:8081.

To read the generated admin password for your first login to the web UI, you can use the command below against the running docker container:

    docker exec -it nexus-repository-puppet cat /nexus-data/admin.password && echo

For simplicity, you should check `Enable anonymous access` in the prompts following your first login.

## Using Puppet With Nexus Repository Manager 3

On how to get started to proxy remote repositories, create hosted ones, upload your private modules and create group of repositories [we have detailed instructions here!](docs/PUPPET_USER_DOCUMENTATION.md)

## Compatibility with Nexus Repository Manager 3 Versions

The table below outlines what version of Nexus Repository the plugin was built against

| Plugin Version | Nexus Repository Version |
|----------------|--------------------------|
| v0.0.1         | 3.14.0-04                |
| v0.0.2         | 3.14.0-04                |
| v0.1.0         | 3.17.0-01                |

If a new version of Nexus Repository is released and the plugin needs changes, a new release will be made, and this
table will be updated to indicate which version of Nexus Repository it will function against. This is done on a time 
available basis, as this is community supported. If you see a new version of Nexus Repository, go ahead and update the
plugin and send us a PR after testing it out!

All released versions can be found [here](https://github.com/sonatype-nexus-community/nexus-repository-puppet/releases).

## Features Implemented In This Plugin 

| Feature | Implemented          |
|---------|----------------------|
| Proxy   | :heavy_check_mark: * |
| Hosted  | :heavy_check_mark:   |
| Group   | :heavy_check_mark:   |

*`*` tested primarily against the Puppet Forge, not guaranteed to work on the wide wild world of Puppet repositories.*

### Supported Puppet Commands

| Plugin Version               | Implemented              |
|------------------------------|--------------------------|
| `puppet module install`      | :heavy_check_mark:       |

## Installing the plugin

There are a range of options for installing the puppet plugin. You'll need to build it first, and
then install the plugin with the options shown below:

### Temporary Install

Installations done via the Karaf console will be wiped out with every restart of Nexus Repository. This is a
good installation path if you are just testing or doing development on the plugin.

* Enable Nexus Repo console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole`  to `true`.

  More details here: [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development+Overview)

* Run Nexus Repo console:
  ```
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-puppet-0.1.0.jar
  > bundle:list
  ```
  (look for org.sonatype.nexus.plugins:nexus-repository-puppet ID, should be the last one)
  ```
  > bundle:start <org.sonatype.nexus.plugins:nexus-repository-puppet ID>
  ```

### (more) Permanent Install

For more permanent installs of the nexus-repository-puppet plugin, follow these instructions:

* Copy the bundle (nexus-repository-puppet-0.1.0.jar) into <nexus_dir>/deploy

This will cause the plugin to be loaded with each restart of Nexus Repository. As well, this folder is monitored
by Nexus Repository and the plugin should load within 60 seconds of being copied there if Nexus Repository
is running. You will still need to start the bundle using the karaf commands mentioned in the temporary install.

### (most) Permanent Install

If you are trying to use the puppet plugin permanently, it likely makes more sense to do the following:

* Copy the bundle into `<nexus_dir>/system/org/sonatype/nexus/plugins/nexus-repository-puppet/0.1.0/nexus-repository-puppet-0.1.0.jar`
* Make the following additions marked with + to `<nexus_dir>/system/org/sonatype/nexus/assemblies/nexus-core-feature/3.x.y/nexus-core-feature-3.x.y-features.xml`

   ```
         <feature prerequisite="false" dependency="false">wrap</feature>
   +     <feature prerequisite="false" dependency="false">nexus-repository-puppet</feature>
   ```
   to the `<feature name="nexus-core-feature" description="org.sonatype.nexus.assemblies:nexus-core-feature" version="3.x.y.xy">` section below the last (above is an example, the exact last one may vary).

   And
   ```
   + <feature name="nexus-repository-puppet" description="org.sonatype.nexus.plugins:nexus-repository-puppet" version="0.1.0">
   +     <details>org.sonatype.nexus.plugins:nexus-repository-puppet</details>
   +     <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-puppet/0.1.0</bundle>
   + </feature>
    </features>
   ```
   as the last feature.
   
This will cause the plugin to be loaded and started with each startup of Nexus Repository.

## The Fine Print

It is worth noting that this is **NOT SUPPORTED** by Sonatype, and is a contribution of ours
to the open source community (read: you!)

Remember:

* Use this contribution at the risk tolerance that you have
* Do NOT file Sonatype support tickets related to puppet support in regard to this plugin
* DO file issues here on GitHub, so that the community can pitch in

Phew, that was easier than I thought. Last but not least of all:

Have fun creating and using this plugin and the Nexus platform, we are glad to have you here!

## Getting help

Looking to contribute to our code but need some help? There's a few ways to get information:

* Chat with us on [Gitter](https://gitter.im/sonatype/nexus-developers)
* Check out the [Nexus3](http://stackoverflow.com/questions/tagged/nexus3) tag on Stack Overflow
* Check out the [Nexus Repository User List](https://groups.google.com/a/glists.sonatype.com/forum/?hl=en#!forum/nexus-users)
