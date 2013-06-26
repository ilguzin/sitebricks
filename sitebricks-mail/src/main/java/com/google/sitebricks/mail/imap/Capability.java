package com.google.sitebricks.mail.imap;

import java.util.List;

/**
 * @author <a href="http://youdev.co">Denis Ilguzin</a>
 */
public class Capability {

  private final List<String> capability;

  public Capability(List<String> capability) {
    this.capability = capability;
  }

  public List<String> getCapability() {
    return capability;
  }

  public boolean hasCapability(String cap) {
    return capability.contains(cap);
  }

}
