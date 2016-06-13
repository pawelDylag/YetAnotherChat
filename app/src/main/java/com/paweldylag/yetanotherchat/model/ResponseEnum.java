package com.paweldylag.yetanotherchat.model;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public enum ResponseEnum {

  AUTHORIZED("Authorized"),
  UNAUTHORIZED("Unauthorized"),
  OK("Ok");

  private String text;

  ResponseEnum(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text;
  }

  public static ResponseEnum fromString(String text) {
    if (text != null) {
      for (ResponseEnum b : ResponseEnum.values()) {
        if (text.equalsIgnoreCase(b.text)) {
          return b;
        }
      }
    }
    return null;
  }
}
