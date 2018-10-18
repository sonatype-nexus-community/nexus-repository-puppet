ARG NEXUS_VERSION=3.14.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.14.0
ARG NEXUS_BUILD=04

COPY . /nexus-repository-puppet/
RUN cd /nexus-repository-puppet/; sed -i "s/3.14.0-04/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.14.0
ARG NEXUS_BUILD=04
ARG PUPPET_VERSION=0.0.1
ARG TARGET_DIR=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-puppet/${PUPPET_VERSION}/
USER root
RUN mkdir -p ${TARGET_DIR}; \
    sed -i 's@nexus-repository-maven</feature>@nexus-repository-maven</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-puppet</feature>@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-maven"@<feature name="nexus-repository-puppet" description="org.sonatype.nexus.plugins:nexus-repository-puppet" version="0.0.1">\n        <details>org.sonatype.nexus.plugins:nexus-repository-puppet</details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-puppet/0.0.1</bundle>\n        <bundle>mvn:org.apache.commons/commons-compress/1.16</bundle>\n   </feature>\n    <feature name="nexus-repository-maven"@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-puppet/target/nexus-repository-puppet-${PUPPET_VERSION}.jar ${TARGET_DIR}
USER nexus
