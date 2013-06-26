package com.google.sitebricks.mail.imap;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author <a href="http://youdev.co">Denis Ilguzin</a>
 */
class GetCapabilityExtractor implements Extractor<Capability> {

  public static final String CAPABILITY_PREFIX = "* CAPABILITY";
  static final Pattern CAPABILITY_SUCCESS_REGEX = Pattern.compile("[.]\\s+OK.*", Pattern.CASE_INSENSITIVE);


  @Override
  public Capability extract(List<String> messages) {

    List<String> capability = null;

    for (String message : messages) {

      if (message.startsWith(CAPABILITY_PREFIX)) {
        capability = Arrays.asList(message.substring(CAPABILITY_PREFIX.length() + 1).split("[ ]+"));
      }

    }

    Preconditions.checkNotNull(capability, "Error parsing capability messages " + messages);

    return new Capability(capability);
  }
}
