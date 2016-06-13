package com.paweldylag.yetanotherchat.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class Contact {

  @SerializedName("name")
  public final String name;
  @SerializedName("online")
  public final boolean online;
  @SerializedName("info")
  public final String description;

  public Contact(String name, boolean online, String description) {
    this.name = name;
    this.online = online;
    this.description = description;
  }

}
