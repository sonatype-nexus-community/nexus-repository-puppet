ARG NEXUS_VERSION=3.17.0
ARG NEXUS_BUILD=01
ARG PUPPET_VERSION=0.1.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION
ARG NEXUS_BUILD
RUN apk add git
RUN git clone https://github.com/sonatype/nexus-public.git --branch release-${NEXUS_VERSION}-${NEXUS_BUILD}
WORKDIR /nexus-public
RUN mvn -V clean verify -DskipTests -T1C

COPY . /nexus-repository-puppet/
WORKDIR /nexus-repository-puppet/
RUN mvn -V clean package

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION
ARG NEXUS_BUILD
ARG PUPPET_VERSION
ARG TARGET_DIR=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-puppet/${PUPPET_VERSION}/
USER root
RUN mkdir -p ${TARGET_DIR}; \
    sed -i 's@nexus-repository-maven</feature>@nexus-repository-maven</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-puppet</feature>@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i "s@<feature name=\"nexus-repository-maven\"@<feature name=\"nexus-repository-puppet\" description=\"org.sonatype.nexus.plugins:nexus-repository-puppet\" version=\"${PUPPET_VERSION}\">\n        <details>org.sonatype.nexus.plugins:nexus-repository-puppet</details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-puppet/${PUPPET_VERSION}</bundle>\n        </feature>\n    <feature name=\"nexus-repository-maven\"@g" /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-puppet/target/nexus-repository-puppet-${PUPPET_VERSION}.jar ${TARGET_DIR}
USER nexus
