/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.puppet.internal.metadata;

public class ModuleReleasesResult
{
  private String uri;

  private String slug;

  private String version;

  private String file_uri;

  private String file_md5;

  private ModuleMetadata metadata;

  public ModuleMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(final ModuleMetadata metadata) {
    this.metadata = metadata;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(final String uri) {
    this.uri = uri;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(final String slug) {
    this.slug = slug;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getFile_uri() {
    return file_uri;
  }

  public void setFile_uri(final String file_uri) {
    this.file_uri = file_uri;
  }

  public String getFile_md5() {
    return file_md5;
  }

  public void setFile_md5(final String file_md5) {
    this.file_md5 = file_md5;
  }
  /*
  private Module module;

  public class Module
  {
    private String uri;
    private String slug;
    private String name;
    private String deprecated_at;
    private Owner owner;

    public class Owner
    {
      //private String
    }
  }
  */
/*
  {
    "uri": "/v3/releases/yo61-logrotate-999.999.999",
      "slug": "yo61-logrotate-999.999.999",
      "module": {
    "uri": "/v3/modules/yo61-logrotate",
        "slug": "yo61-logrotate",
        "name": "logrotate",
        "deprecated_at": "2017-10-17 07:38:00 -0700",
        "owner": {
      "uri": "/v3/users/yo61",
          "slug": "yo61",
          "username": "yo61",
          "gravatar_id": "e29d5d8ef85cb457c1bbd0565ce5cf87"
    }
  },
    "version": "999.999.999",
      "metadata": {
    "name": "yo61-logrotate",
        "version": "999.999.999",
        "author": "Robin Bowees <robin.bowes@yo61.com>",
        "summary": "Manage logrotate",
        "license": "MIT",
        "source": "https://github.com/yo61/puppet-logrotate",
        "project_page": "https://github.com/yo61/puppet-logrotate",
        "issues_url": "https://github.com/yo61/puppet-logrotate/issues",
        "dependencies": [
    {
      "name": "puppetlabs/stdlib",
        "version_requirement": ">= 1.0.0 <5.0.0"
    }
        ],
    "data_provider": null,
        "tags": [
    "logrotate"
        ],
    "requirements": [
    {
      "name": "puppet",
        "version_requirement": ">= 3.2.0 < 5.0.0"
    },
    {
      "name": "pe",
        "version_requirement": ">= 3.2.0 < 2015.4.0"
    }
        ],
    "operatingsystem_support": [
    {
      "operatingsystem": "CentOS",
        "operatingsystemrelease": [
      "5",
          "6",
          "7"
            ]
    },
    {
      "operatingsystem": "Debian",
        "operatingsystemrelease": [
      "6",
          "7"
            ]
    },
    {
      "operatingsystem": "OracleLinux",
        "operatingsystemrelease": [
      "5",
          "6",
          "7"
            ]
    },
    {
      "operatingsystem": "RedHat",
        "operatingsystemrelease": [
      "5",
          "6",
          "7"
            ]
    },
    {
      "operatingsystem": "Scientific",
        "operatingsystemrelease": [
      "5",
          "6",
          "7"
            ]
    },
    {
      "operatingsystem": "Ubuntu",
        "operatingsystemrelease": [
      "10.04",
          "12.04",
          "14.04"
            ]
    },
    {
      "operatingsystem": "Gentoo",
        "operatingsystemrelease": [
      "1.0"
            ]
    }
        ]
  },
    "tags": [
    "logrotate"
      ],
    "supported": false,
      "pdk": false,
      "validation_score": 92,
      "file_uri": "/v3/files/yo61-logrotate-999.999.999.tar.gz",
      "file_size": 19928,
      "file_md5": "155fc2101fcac3b83ecee4ecacab6129",
      "downloads": 222561,
      "readme": "# Logrotate module for Puppet\n\nThis module has been migrated to the Voxpupuli organisation.\n\nIt can be found at https://forge.puppet.com/puppet/logrotate\n\nThanks,\n\nRobin Bowes (yo61)\nMay, 2017\n",
      "changelog": "2016-05-30 Release 1.4.0\n- workaround for bug PUP-6336\n- add ability to override default btmp and/or wtmp\n- add puppet 4 support, drop puppet 2.7 support\n- minor cleanups\n\n2015-11-05 Release 1.3.0\n- set default package version to \"present\" rather than \"latest\" (#11) (natemccurdy)\n- add documentation for setting class defaults (natemccurdy)\n\n2015-09-14 Release 1.2.8\n- Fix hidden unicode character (#8)\n- Allow config to be passed in as an hash (#6)\n- Fix dependency issue (#7)\n- refactor main class (mostly to facilitate #7)\n- update test environment to use puppet 4\n- switch stdlib fixture to https source\n\n2015-05-06 Release 1.2.7\n- Metadata-only release (just bumped version)\n\n2015-05-06 Release 1.2.6\n- Fix test failures on future parser\n\n2015-05-06 Release 1.2.5\n- Switch some validation code to use validate_re\n\n2015-05-06 Release 1.2.4\n- Add puppet-lint exclusions\n\n2015-05-06 Release 1.2.3\n- More work on testing\n- fix warning when running puppet module list caused by \"-\" instead of \"/\" in\ndependencies in metadata\n\n2015-05-06 Release 1.2.3\n- removed (pushed without CHANGELOG update\n\n2015-05-06 Release 1.2.1\n- Update tests, Rakefile, etc.\n\n2015-03-25 Release 1.2.0\n- First release to puppetforge",
      "license": "Copyright (c) 2012 Tim Sharpe\n\nPermission is hereby granted, free of charge, to any person obtaining\na copy of this software and associated documentation files (the\n\"Software\"), to deal in the Software without restriction, including\nwithout limitation the rights to use, copy, modify, merge, publish,\ndistribute, sublicense, and/or sell copies of the Software, and to\npermit persons to whom the Software is furnished to do so, subject to\nthe following conditions:\n\nThe above copyright notice and this permission notice shall be\nincluded in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND,\nEXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF\nMERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.\nIN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY\nCLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,\nTORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE\nSOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n",
      "reference": null,
      "tasks": [],
    "created_at": "2017-05-25 12:39:40 -0700",
      "updated_at": "2017-05-25 12:40:09 -0700",
      "deleted_at": null,
      "deleted_for": null
  },
*/
}
