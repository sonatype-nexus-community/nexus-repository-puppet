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
## Puppet Repositories

### Introduction

[Puppet](https://www.puppet.com/) is a application management format used to help automate and run an organizations 
infrastructure, using Puppet Modules, which are effectively recipes for running an application. 

### Proxying The Puppet Forge

You can set up a Puppet proxy repository to access a remote repository location, for example to proxy the stable Puppet
Modules at [the Puppet Forge](https://forge.puppet.com/)

To proxy a Puppet repository, you simply create a new 'puppet (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. [https://forgeapi.puppet.com/](https://forgeapi.puppet.com/)
- Select a 'Blob store' for 'Storage'

### Configuring Puppet 

There are relatively few steps for configuring Puppet to use Nexus Repository:

You'll need to have Puppet installed, for interaction with the Puppet Forge.

NOTE: These instructions are Linux/OS X specific.

Once you have Puppet up and running you'll want to run commands similar to the following:

You'll need to create a `puppet.conf` file at the following location if it does not exist:

`~/.puppet/puppet.conf`

In this file you'll need to add:

`module_repository = 'http://nexushostname:nexusport/repository/puppet-proxy-name'`

Once you've done this, you should be ready to install Puppet modules via `puppet install module`!

### Browsing Puppet Repository Packages

You can browse Puppet repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).
