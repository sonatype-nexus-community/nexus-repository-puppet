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

public class ModulePagination
{
  private long limit;

  private long offset;

  private String first;

  private String previous;

  private String current;

  private String next;

  private long total;

  public long getLimit() {
    return limit;
  }

  public void setLimit(final long limit) {
    this.limit = limit;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(final long offset) {
    this.offset = offset;
  }

  public String getFirst() {
    return first;
  }

  public void setFirst(final String first) {
    this.first = first;
  }

  public String getPrevious() {
    return previous;
  }

  public void setPrevious(final String previous) {
    this.previous = previous;
  }

  public String getCurrent() {
    return current;
  }

  public void setCurrent(final String current) {
    this.current = current;
  }

  public String getNext() {
    return next;
  }

  public void setNext(final String next) {
    this.next = next;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(final long total) {
    this.total = total;
  }
}
