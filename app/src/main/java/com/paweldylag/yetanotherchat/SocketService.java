package com.paweldylag.yetanotherchat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paweldylag.yetanotherchat.model.Contact;
import com.paweldylag.yetanotherchat.model.Message;
import com.paweldylag.yetanotherchat.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class SocketService {

  private static final String TAG = "SocketService";

  private static final String OP_LOGIN = "login";
  private static final String OP_LOGOUT = "logout";
  private static final String OP_REGISTER = "register";
  private static final String OP_MESSAGE = "message";
  private static final String OP_MESSAGE_HISTORY = "message_history";
  private static final String OP_GET_CONTACTS = "get_contacts";
  private static final String OP_ADD_CONTACT = "add_contact";
  private static final String OP_GET_ALL_USERS = "get_users";
  private static final String OP_CHANGE_DESCRIPTION = "change_description";

  public interface Callback {
    void onSuccess(Object response);
    void onFailure(String message);
  }

  public static SocketService getInstance(){
      if (INSTANCE == null) {
        INSTANCE = new SocketService();
      }
      return INSTANCE;
  }
  private static SocketService INSTANCE;

  private Socket socket;
  private Callback responseListener;
  private Gson gson = new GsonBuilder().create();

  private Emitter.Listener onResponseListener = new Emitter.Listener() {
    @Override
    public void call(Object... args) {
      JSONObject data = (JSONObject) args[0];
      Response r = gson.fromJson(data.toString(), Response.class);
      if (responseListener != null) {
        switch(r.operation) {
          case OP_LOGIN :
            if (r.message.equals("success")){
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("Wrong password or username.");
            }
            break;
          case OP_REGISTER :
            if (r.message.equals("success")){
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("User already exists.");
            }
            break;
          case OP_GET_CONTACTS:
            if (r.message.equals("success")) {
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("Cannot fetch user's contacts");
            }
            break;
          case OP_GET_ALL_USERS:
            if (r.message.equals("success")) {
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("Cannot fetch all users");
            }
            break;
          case OP_MESSAGE_HISTORY:
            if (r.message.equals("success")) {
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("Cannot fetch all users");
            }
            break;
          case OP_CHANGE_DESCRIPTION:
            if (r.message.equals("success")) {
              responseListener.onSuccess(r);
            } else {
              responseListener.onFailure("Cannot change description");
            }
            break;

        }
        responseListener = null;
      } else {
        Log.d(TAG, "Not handled response.");
      }
    }
  };




  private SocketService () {
  }

  /**
   * Inits socket connection
   */
  public void connect(){
    socket.connect();
    socket.on(OP_LOGIN, onResponseListener);
    socket.on(OP_REGISTER, onResponseListener);
    socket.on(OP_GET_CONTACTS, onResponseListener);
    socket.on(OP_GET_ALL_USERS, onResponseListener);
    socket.on(OP_MESSAGE_HISTORY, onResponseListener);
    socket.on(OP_CHANGE_DESCRIPTION, onResponseListener);
}

  public boolean attemptRegister(final String username, final String password, final Callback callback) {
    if (socket == null) {
      callback.onFailure("Unresolved host.");
      return false;
    }
    if(!socket.connected()) {
      connect();
    }
    responseListener = callback;
    socket.emit(OP_REGISTER, username, password);
    return true;
  }

  public boolean attemptLogin(final String username, final String password, final Callback callback) {
    if (socket == null) {
      callback.onFailure("Unresolved host.");
      return false;
    }
    if(!socket.connected()) {
      connect();
    }
    responseListener = callback;
    socket.emit(OP_LOGIN, username, password);
    return true;
  }

  public void getContacts(final String username, Callback callback) {
    Log.d(TAG, "Getting contacts for " + username);
    responseListener = callback;
    socket.emit(OP_GET_CONTACTS, username);
  }

  public void getAllUsers(Callback callback) {
    responseListener = callback;
    socket.emit(OP_GET_ALL_USERS);
  }

  public void addNewContact(final String contactName) {
    socket.emit(OP_ADD_CONTACT, contactName);
  }

  public void registerMessageListener(Emitter.Listener listener) {
    Log.d(TAG, "Registering listener");
    socket.off(OP_MESSAGE);
    socket.on(OP_MESSAGE, listener);
  }

  public void unregisterMessageListener() {
    Log.d(TAG, "Unregistering listener.");
    socket.off(OP_MESSAGE);
  }

  public void changeDescription(String newDescription, Callback callback) {
    responseListener = callback;
    socket.emit(OP_CHANGE_DESCRIPTION, newDescription);
  }

  public void getMessageHistory(final String user, final String contact, Callback callback) {
    responseListener = callback;
    socket.emit(OP_MESSAGE_HISTORY, user, contact);
  }

  public void sendMessage(final String from, final String to, final String message) {
    socket.emit(OP_MESSAGE, from, to, message);
  }

  public void logout(){
    socket.emit(OP_LOGOUT);
    socket.disconnect();
  }

  public boolean setEndpoint(String endpoint) {
    try {
      socket = IO.socket(endpoint);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return false;
    }
    return true;

  }

}
