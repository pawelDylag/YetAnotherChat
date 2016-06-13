package com.paweldylag.yetanotherchat.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class Response {

  @SerializedName("operation")
  public String operation;
  @SerializedName("message")
  public String message;
  @SerializedName("data")
  public List<Contact> data;
  @SerializedName("history")
  public List<Message> history;

}
