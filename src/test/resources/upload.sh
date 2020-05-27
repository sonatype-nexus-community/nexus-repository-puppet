#!/bin/bash
#
# This uploads Puppet Modules in the current directory to a Puppet hosted repo
#

find . -exec curl -X PUT \
               -v \
               --upload-file {} \
               http://localhost:8081/repository/puppet-hosted/{} \
               -H 'Authorization: Basic YWRtaW46YWRtaW4xMjM=' \;
