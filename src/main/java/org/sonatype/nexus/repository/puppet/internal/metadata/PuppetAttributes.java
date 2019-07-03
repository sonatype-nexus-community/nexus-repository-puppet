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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @since 0.0.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PuppetAttributes
{
  private String name;

  private String version;

  private String summary;

  private String description;

  private String author;

  private String license;

  private String source;

  private String project_page;

  private String issues_url;

  private List<PuppetDependencyAttributes> dependencies;

  public String getAuthor() {
    return author;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(final String license) {
    this.license = license;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public String getProject_page() {
    return project_page;
  }

  public void setProject_page(final String project_page) {
    this.project_page = project_page;
  }

  public String getIssues_url() {
    return issues_url;
  }

  public void setIssues_url(final String issues_url) {
    this.issues_url = issues_url;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(final String summary) {
    this.summary = summary;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public List<PuppetDependencyAttributes> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<PuppetDependencyAttributes> dependencies) {
    this.dependencies = dependencies;
  }
}
