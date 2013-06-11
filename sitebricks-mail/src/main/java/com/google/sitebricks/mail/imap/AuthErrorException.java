package com.google.sitebricks.mail.imap;

/**
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class AuthErrorException extends Exception {
  public AuthErrorException(String message) {
    super(message);
  }
}
