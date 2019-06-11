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
# Puppet Repositories

## Introduction

[Puppet](https://www.puppet.com/) is a application management format used to help automate and run an organizations 
infrastructure, using Puppet Modules, which are effectively recipes for running an application. 

## Configuring Puppet repositories

You can configure all three types of repositories supported by Nexus Repository Manger: proxy, hosted, and group. Below are instruction for each one.

### Proxying The Puppet Forge

You can set up a Puppet proxy repository to access a remote repository location, for example to proxy the stable Puppet
Modules at [the Puppet Forge](https://forge.puppet.com/)

To proxy a Puppet repository, you simply create a new *'puppet (proxy)'* as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. [https://forgeapi.puppet.com/](https://forgeapi.puppet.com/)
- Select a 'Blob store' for 'Storage'

### Hosting private modules

You can set up a Puppet hosted repository to hold your private modules.

To host your modules, you simply create a new *'puppet (hosted)'* as documented in [Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in detail. Minimal configuration steps are:

 - Define 'Name'
 - Select a 'Blob store' for 'Storage'
 
#### Publishing private modules

For now there is no UI for publishing modules. You need to run commands similar to the following:

```bash
# Build your private module
pdk build
# Upload with cURL
curl -v \
  -u admin:admin123 \
  -T pkg/acmecorp-sample-0.1.0.tar.gz \
  http://nexus:8081/repository/puppet-hosted/acmecorp-sample-0.1.0.tar.gz
```

### Grouping Puppet repositories

You can set up a Puppet group repository to merge content from multiple other puppet repositories.

To group your repositories, you simply create a new *'puppet (group)'* as documented in [Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in detail. Minimal configuration steps are:

 - Define 'Name'
 - Select repositories that should form a group, and define their order
 - Select a 'Blob store' for 'Storage'
 
 **CAUTION!** Grouped repositories will be search in order defined. If some repository contain a given module for ex.: `acmecorp/sample`, other repositories will not be checked for other versions of that module. That's true, even if there can be some other versions on different repositories. This behavior might improve in some time as it's not ideal.

## Configuring Puppet 

There are relatively few steps for configuring Puppet to use Nexus Repository.

You'll need to have Puppet installed, for interaction with the Puppet Repositories.

*NOTE: These instructions are Linux/OS X specific, but configuration is similar on Windows.*

Once you have Puppet up and running you'll want to run commands similar to the following:

```bash
puppet module install \
  --module_repository http://nexus-host:port/repository/puppet-repo-name \
  puppetlabs-kubernetes
```

You can also configure that Nexus will be your default repository. Once you've done this, you should be ready to install Puppet modules via `puppet module install` without using `module_repository` command line option!

You'll need to create (or edit) a `puppet.conf` file at the following locations:

* `~/.puppet/puppet.conf` for users other then root
* `/etc/puppetlabs/puppet/puppet.conf` for root

In this file you'll need to add in section `[main]`:

```ini
[main]
module_repository = 'http://nexus-host:port/repository/puppet-repo-name'
```

