package com.paweldylag.yetanotherchat.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class Message {

  @SerializedName("from")
  public String from;
  @SerializedName("to")
  public String to;
  @SerializedName("text")
  public String text;
  @SerializedName("timestamp")
  public Long timestamp;

  public Message(String from, String to, String text) {
    this.from = from;
    this.to = to;
    this.text = text;
  }
}
